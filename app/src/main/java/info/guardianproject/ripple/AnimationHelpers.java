package info.guardianproject.ripple;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.annotation.SuppressLint;
import android.os.Build;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

@SuppressLint("NewApi")
public class AnimationHelpers {

    public static void translateY(final View view, float fromY, float toY, long duration) {
        if (Build.VERSION.SDK_INT >= 12) {
            if (duration == 0)
                view.setTranslationY(toY);
            else
                view.animate().translationY(toY).setDuration(duration).start();
        } else {
            TranslateAnimation translate = new TranslateAnimation(0, 0, fromY, toY);
            translate.setDuration(duration);
            translate.setFillEnabled(true);
            translate.setFillBefore(true);
            translate.setFillAfter(true);
            addAnimation(view, translate);
        }
    }

    public static void scale(final View view, float fromScale, float toScale, long duration, final Runnable whenDone) {
        if (Build.VERSION.SDK_INT >= 12) {
            if (duration == 0) {
                view.setScaleX(toScale);
                view.setScaleY(toScale);
                if (whenDone != null)
                    whenDone.run();
            } else {
                ViewPropertyAnimator animation = view.animate().scaleX(toScale).scaleY(toScale).setDuration(duration);
                if (whenDone != null) {
                    animation.setListener(new AnimatorListener() {
                        @Override
                        public void onAnimationCancel(Animator animation) {
                            whenDone.run();
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            whenDone.run();
                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {
                        }

                        @Override
                        public void onAnimationStart(Animator animation) {
                        }

                    });
                }
                animation.start();
            }
        } else {
            ScaleAnimation scale = new ScaleAnimation(fromScale, toScale, fromScale, toScale, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                    0.5f);
            scale.setDuration(duration);
            scale.setFillEnabled(true);
            scale.setFillBefore(true);
            scale.setFillAfter(true);

            if (whenDone != null) {
                scale.setAnimationListener(new AnimationListener() {
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        whenDone.run();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                });
            }
            addAnimation(view, scale);
        }
    }

    static void addAnimation(View view, Animation animation) {
        addAnimation(view, animation, false);
    }

    static void addAnimation(View view, Animation animation, boolean first) {
        Animation previousAnimation = view.getAnimation();
        if (previousAnimation == null || previousAnimation.getClass() == animation.getClass()) {
            if (animation.getStartTime() == Animation.START_ON_FIRST_FRAME)
                view.startAnimation(animation);
            else
                view.setAnimation(animation);
            return;
        }

        if (!(previousAnimation instanceof AnimationSet)) {
            AnimationSet newSet = new AnimationSet(false);
            newSet.addAnimation(previousAnimation);
            previousAnimation = newSet;
        }

        // Remove old of same type
        //
        AnimationSet set = (AnimationSet) previousAnimation;
        for (int i = 0; i < set.getAnimations().size(); i++) {
            Animation anim = set.getAnimations().get(i);
            if (anim.getClass() == animation.getClass()) {
                set.getAnimations().remove(i);
                break;
            }
        }

        // Add this (first if it is a scale animation ,else at end)
        if (animation instanceof ScaleAnimation || first) {
            set.getAnimations().add(0, animation);
        } else {
            set.getAnimations().add(animation);
        }

        animation.startNow();
        view.setAnimation(set);
    }
}
