package tslamic.github.com.delightfulmenudrawable;

import android.animation.Animator;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

public class ShowcaseActivity extends Activity {

    private DelightfulMenuDrawable mDrawable;
    private EditText mAnimDuration;
    private CompoundButton mRtl;
    private int mAnimTypePosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.showcase);

        mDrawable = new DelightfulMenuDrawable(this);
        mAnimDuration = (EditText) findViewById(R.id.anim_duration);
        mRtl = (CompoundButton) findViewById(R.id.is_rtl);

        final Spinner animTypes = (Spinner) findViewById(R.id.anim_type);
        animTypes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mAnimTypePosition = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mAnimTypePosition = 0;
            }
        });

        final ImageView imageView = (ImageView) findViewById(R.id.img);
        imageView.setImageDrawable(mDrawable);
    }

    public void onAnimate(View v) {
        final String duration = mAnimDuration.getText().toString();
        mDrawable.setAnimation(DelightfulMenuDrawable.Animation.values()[mAnimTypePosition]);
        mDrawable.setRtlLayoutDirection(mRtl.isChecked());
        Animator animator = mDrawable.getAnimator(Integer.parseInt(duration));
        animator.start();
    }

}
