package info.guardianproject.securereaderinterface.uiutil;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.annotation.SuppressLint;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewPropertyAnimator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

@SuppressLint("NewApi")
public class AnimationHelpers
{
	private static final int compatibility_translation_x_key = 0xaddf;

	public interface FadeInFadeOutListener
	{
		void onFadeInStarted(View view);

		void onFadeInEnded(View view);

		void onFadeOutStarted(View view);

		void onFadeOutEnded(View view);
	}

	/**
	 * Fade the alpha of the view to 0.
	 * 
	 * @param view
	 *            The view to fade
	 * @param duration
	 *            Number of ms for the animation
	 * @param startDelay
	 *            Optional start delay in ms
	 * @param removeFromParent
	 *            true to remove the view from parent after animation, false to
	 *            just hide it
	 */
	public static void fadeOut(final View view, int duration, int startDelay, final boolean removeFromParent)
	{
		AnimationHelpers.fadeOut(view, duration, startDelay, removeFromParent, null);
	}

	/**
	 * Fade the alpha of the view to 0.
	 * 
	 * @param view
	 *            The view to fade
	 * @param duration
	 *            Number of ms for the animation
	 * @param startDelay
	 *            Optional start delay in ms
	 * @param removeFromParent
	 *            true to remove the view from parent after animation, false to
	 *            just hide it
	 * @param listener
	 *            Optional listener to receive animation events
	 */
	public static void fadeOut(final View view, int duration, int startDelay, final boolean removeFromParent, final FadeInFadeOutListener listener)
	{
		if (Build.VERSION.SDK_INT >= 14)
		{
			if (duration == 0 && startDelay == 0)
			{
				if (listener != null)
					listener.onFadeOutStarted(view);
				view.setAlpha(0);
				if (removeFromParent)
					((ViewGroup) view.getParent()).removeView(view);
				if (listener != null)
					listener.onFadeOutEnded(view);
			}
			else
			{
				view.animate().alpha(0).setDuration(duration).setStartDelay(startDelay).setListener(new AnimatorListener()
				{
					@Override
					public void onAnimationCancel(Animator animation)
					{
						view.setVisibility(View.GONE);
						if (removeFromParent)
							((ViewGroup) view.getParent()).removeView(view);
					}

					@Override
					public void onAnimationEnd(Animator animation)
					{
						view.setVisibility(View.GONE);
						if (removeFromParent)
							((ViewGroup) view.getParent()).removeView(view);
						if (listener != null)
							listener.onFadeOutEnded(view);
					}

					@Override
					public void onAnimationRepeat(Animator animation)
					{
					}

					@Override
					public void onAnimationStart(Animator animation)
					{
						if (listener != null)
							listener.onFadeOutStarted(view);
					}
				});
			}
		}
		else
		{
			AlphaAnimation alpha = new AlphaAnimation((duration == 0) ? 0 : 1.0f, 0);
			alpha.setDuration(duration);
			if (startDelay > 0)
				alpha.setStartTime(AnimationUtils.currentAnimationTimeMillis() + startDelay);
			alpha.setFillAfter(true);
			alpha.setAnimationListener(new AnimationListener()
			{
				@Override
				public void onAnimationEnd(Animation animation)
				{
					view.setVisibility(View.GONE);
					if (removeFromParent)
						((ViewGroup) view.getParent()).removeView(view);
					if (listener != null)
						listener.onFadeOutEnded(view);
				}

				@Override
				public void onAnimationRepeat(Animation animation)
				{
				}

				@Override
				public void onAnimationStart(Animation animation)
				{
					if (listener != null)
						listener.onFadeOutStarted(view);
				}
			});
			addAnimation(view, alpha);
		}
	}

	public static void fadeIn(final View view, final int duration, final int fadeOutDelay, final boolean removeFromParent)
	{
		AnimationHelpers.fadeIn(view, duration, fadeOutDelay, removeFromParent, null);
	}

	public static void fadeIn(final View view, final int duration, final int fadeOutDelay, final boolean removeFromParent, final FadeInFadeOutListener listener)
	{
		if (Build.VERSION.SDK_INT >= 14)
		{
			view.animate().alpha(1).setDuration(duration).setStartDelay(0).setListener(new AnimatorListener()
			{
				@Override
				public void onAnimationCancel(Animator animation)
				{
				}

				@Override
				public void onAnimationEnd(Animator animation)
				{
					if (listener != null)
						listener.onFadeInEnded(view);
					if (fadeOutDelay > 0)
						fadeOut(view, duration, fadeOutDelay, removeFromParent, listener);
				}

				@Override
				public void onAnimationRepeat(Animator animation)
				{
				}

				@Override
				public void onAnimationStart(Animator animation)
				{
					if (listener != null)
						listener.onFadeInStarted(view);
				}
			});
		}
		else
		{
			AlphaAnimation alpha = new AlphaAnimation((duration == 0) ? 1 : 0, 1);
			alpha.setDuration(duration);
			alpha.setFillAfter(true);
			alpha.setAnimationListener(new AnimationListener()
			{
				@Override
				public void onAnimationEnd(Animation animation)
				{
					if (listener != null)
						listener.onFadeInEnded(view);
					if (fadeOutDelay > 0)
						fadeOut(view, duration, fadeOutDelay, removeFromParent);
				}

				@Override
				public void onAnimationRepeat(Animation animation)
				{
				}

				@Override
				public void onAnimationStart(Animation animation)
				{
					if (listener != null)
						listener.onFadeInStarted(view);
				}
			});
			addAnimation(view, alpha);
		}
	}

	public static void rotate(final View view, float fromDegrees, float toDegrees, long duration)
	{
		if (Build.VERSION.SDK_INT >= 12)
		{
			if (duration == 0)
				view.setRotation(toDegrees);
			else
				view.animate().rotation(toDegrees).setDuration(duration).start();
		}
		else
		{
			RotateAnimation rotate = new RotateAnimation(fromDegrees, toDegrees, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
			rotate.setDuration(duration);
			rotate.setFillEnabled(true);
			rotate.setFillBefore(true);
			rotate.setFillAfter(true);
			addAnimation(view, rotate);
		}
	}

	public static void translateY(final View view, float fromY, float toY, long duration)
	{
		if (Build.VERSION.SDK_INT >= 12)
		{
			if (duration == 0)
				view.setTranslationY(toY);
			else
				view.animate().translationY(toY).setDuration(duration).start();
		}
		else
		{
			TranslateAnimation translate = new TranslateAnimation(0, 0, fromY, toY);
			translate.setDuration(duration);
			translate.setFillEnabled(true);
			translate.setFillBefore(true);
			translate.setFillAfter(true);
			addAnimation(view, translate);
		}
	}

	public static void translateX(final View view, float fromX, float toX, long duration)
	{
		if (Build.VERSION.SDK_INT >= 12)
		{
			if (duration == 0)
				view.setTranslationX(toX);
			else
				view.animate().translationX(toX).setDuration(duration).start();
		}
		else
		{
			TranslateAnimation translate = new TranslateAnimation(fromX, toX, 0, 0);
			translate.setDuration(duration);
			translate.setFillEnabled(true);
			translate.setFillBefore(true);
			translate.setFillAfter(true);
			addAnimation(view, translate);
		}
	}

	public static void scale(final View view, float fromScale, float toScale, long duration, final Runnable whenDone)
	{
		if (Build.VERSION.SDK_INT >= 12)
		{
			if (duration == 0)
			{
				view.setScaleX(toScale);
				view.setScaleY(toScale);
				if (whenDone != null)
					whenDone.run();
			}
			else
			{
				ViewPropertyAnimator animation = view.animate().scaleX(toScale).scaleY(toScale).setDuration(duration);
				if (whenDone != null)
				{
					animation.setListener(new AnimatorListener()
					{
						@Override
						public void onAnimationCancel(Animator animation)
						{
							whenDone.run();
						}

						@Override
						public void onAnimationEnd(Animator animation)
						{
							whenDone.run();
						}

						@Override
						public void onAnimationRepeat(Animator animation)
						{
						}

						@Override
						public void onAnimationStart(Animator animation)
						{
						}

					});
				}
				animation.start();
			}
		}
		else
		{
			ScaleAnimation scale = new ScaleAnimation(fromScale, toScale, fromScale, toScale, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
					0.5f);
			scale.setDuration(duration);
			scale.setFillEnabled(true);
			scale.setFillBefore(true);
			scale.setFillAfter(true);

			if (whenDone != null)
			{
				scale.setAnimationListener(new AnimationListener()
				{
					@Override
					public void onAnimationEnd(Animation animation)
					{
						whenDone.run();
					}

					@Override
					public void onAnimationRepeat(Animation animation)
					{
					}

					@Override
					public void onAnimationStart(Animation animation)
					{
					}

				});
			}
			addAnimation(view, scale);
		}
	}

	public static void addAnimation(View view, Animation animation)
	{
		addAnimation(view, animation, false);
	}

	public static void addAnimation(View view, Animation animation, boolean first)
	{
		Animation previousAnimation = view.getAnimation();
		if (previousAnimation == null || previousAnimation.getClass() == animation.getClass())
		{
			if (animation.getStartTime() == Animation.START_ON_FIRST_FRAME)
				view.startAnimation(animation);
			else
				view.setAnimation(animation);
			return;
		}

		if (!(previousAnimation instanceof AnimationSet))
		{
			AnimationSet newSet = new AnimationSet(false);
			newSet.addAnimation(previousAnimation);
			previousAnimation = newSet;
		}

		// Remove old of same type
		//
		AnimationSet set = (AnimationSet) previousAnimation;
		for (int i = 0; i < set.getAnimations().size(); i++)
		{
			Animation anim = set.getAnimations().get(i);
			if (anim.getClass() == animation.getClass())
			{
				set.getAnimations().remove(i);
				break;
			}
		}

		// Add this (first if it is a scale animation ,else at end)
		if (animation instanceof ScaleAnimation || first)
		{
			set.getAnimations().add(0, animation);
		}
		else
		{
			set.getAnimations().add(animation);
		}

		animation.startNow();
		view.setAnimation(set);
	}

	public static void setTranslationX(final View view, int value)
	{
		if (Build.VERSION.SDK_INT >= 11)
		{
			view.setTranslationX(value);
		}
		else
		{
			ViewGroup.MarginLayoutParams params = (MarginLayoutParams) view.getLayoutParams();
			if (params != null)
			{
				Integer translationX = (Integer) view.getTag(compatibility_translation_x_key);
				if (translationX == null)
					translationX = Integer.valueOf(0);
				
				view.setTag(compatibility_translation_x_key, Integer.valueOf(value));

				params.leftMargin = params.leftMargin + value - translationX.intValue();
				params.rightMargin = params.rightMargin - value + translationX.intValue();
				view.setLayoutParams(params);
			}
		}
	}

	public static int getTranslationX(final View view)
	{
		if (Build.VERSION.SDK_INT >= 11)
		{
			return (int) view.getTranslationX();
		}
		else
		{
			Integer translationX = (Integer) view.getTag(compatibility_translation_x_key);
			if (translationX == null)
				return 0;
			return translationX.intValue();
		}
	}
}
