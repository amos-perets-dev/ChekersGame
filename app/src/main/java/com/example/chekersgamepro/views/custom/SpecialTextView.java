package com.example.chekersgamepro.views.custom;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;

import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatTextView;

import com.example.chekersgamepro.R;
import com.example.chekersgamepro.util.DisplayUtil;

import io.reactivex.Observable;
import io.reactivex.internal.functions.Functions;
import io.reactivex.subjects.PublishSubject;

public class SpecialTextView extends AppCompatTextView {

    private final float BORDER_WIDTH_DEFAULT = DisplayUtil.Companion.convertDpToPixel(1f);
    private final float TEXT_SIZE_DEFAULT = DisplayUtil.Companion.convertDpToPixel(20);

    private String text = "";

    private Paint paintText = new Paint();

    private Paint paintBorder = new Paint();

    private Paint paintBackground = new Paint();

    private int lengthLineTriangle = 0;

    private float borderWith;

    private CharSequence mText;
    private int mIndex;
    private long mDelay = getResources().getInteger(R.integer.view_special_text_animate_text_duration_letter); //Default 500ms delay
    private Handler mHandler = new Handler();

    private PublishSubject<Boolean> isSetTextAnimationEnd = PublishSubject.create();


    @RequiresApi(api = Build.VERSION_CODES.O)
    public SpecialTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public SpecialTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SpecialTextView, defStyleAttr, 0);

          // Init Text
        int textColor = typedArray.getColor(R.styleable.SpecialTextView_SpecialTextColor, Color.WHITE);
        float textSize = typedArray.getFloat(R.styleable.SpecialTextView_SpecialTextSize, TEXT_SIZE_DEFAULT);

        // Init border
        this.borderWith = typedArray.getFloat(R.styleable.SpecialTextView_SpecialBorderWidth, BORDER_WIDTH_DEFAULT);
        int borderColor = typedArray.getColor(R.styleable.SpecialTextView_SpecialBorderWidth, Color.BLACK);

        // Init background
        int backgroundColor = typedArray.getColor(R.styleable.SpecialTextView_SpecialBorderWidth
                , getResources().getColor(R.color.color_dark_gray_50_alpha));

        this.paintText.setColor(textColor);
        this.paintText.setStyle(Paint.Style.FILL);
        this.paintText.setAntiAlias(true);
//        this.paintText.setTextSize(textSize);
        this.paintText.setTypeface(Typeface.SERIF);
        this.paintText.setShadowLayer(10, 1, 1, Color.BLACK);

//        this.paintBorder.setStrokeWidth(borderWith);
        this.paintBorder.setStyle(Paint.Style.STROKE);
        this.paintBorder.setAntiAlias(true);
        this.paintBorder.setColor(borderColor);
        this.paintBorder.setShadowLayer(10, 1, 2, Color.WHITE);

        this.paintBackground.setStyle(Paint.Style.FILL);
        this.paintBackground.setAntiAlias(true);
        this.paintBackground.setColor(backgroundColor);
        typedArray.recycle();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public SpecialTextView(Context context) {
        this(context, null);
    }

    public void setText(String text) {
        this.text = text;
        invalidate();
    }

    public void setTextWithAnimate(String text) {
//        Log.d("TEST_GAME", "SpecialTextView -> setTextWithAnimate");
        animateText(text);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Rect clipBounds = canvas.getClipBounds();
        int width = clipBounds.width();
        int height = clipBounds.height();

        // Draw triangle<<<

        int x = (width / 2);

        this.lengthLineTriangle = (int) (width * 0.035);

        Point startPoint = new Point(x, 0);

        Point a = new Point(x, 0);
        Point b = new Point(x - this.lengthLineTriangle, this.lengthLineTriangle);
        Point c = new Point(x + this.lengthLineTriangle, this.lengthLineTriangle);

        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        path.moveTo(startPoint.x, startPoint.y); // this should set the start point right
        path.lineTo(b.x, b.y);
        path.lineTo(c.x, c.y);
        path.lineTo(a.x, a.y);
        path.close();

        canvas.drawPath(path, this.paintBackground);
        // Draw triangle>>>

        // Draw Rect<<<
        Rect largeRect = new Rect(
                clipBounds.left
                , (int) (clipBounds.top + this.lengthLineTriangle + (this.lengthLineTriangle * 0.02))
                , clipBounds.right
                , clipBounds.bottom);
        canvas.drawRect(largeRect, this.paintBackground);
        // Draw Rect>>>

        // Draw border<<<
        this.paintBorder.setStrokeWidth((float) ((clipBounds.width() * clipBounds.height()) * 0.00002));

        float strokeMargin = this.borderWith / 2;
        double marginTop = this.borderWith * 0.5;
        Point startPointBorder = new Point((int) (0 + strokeMargin), (int) (this.lengthLineTriangle + marginTop));

        Point b1 = new Point(x - (this.lengthLineTriangle /*/ 2*/), (int) (this.lengthLineTriangle + marginTop));
        Point c1 = new Point(x, (int) (0 + (marginTop * 1.4)));
        Point d1 = new Point(x + this.lengthLineTriangle, (int) (this.lengthLineTriangle + marginTop));
        Point e1 = new Point((int) (width - strokeMargin), (int) (this.lengthLineTriangle + marginTop));

        Point f1 = new Point((int) (width - strokeMargin), (int) (height - strokeMargin));
        Point g1 = new Point((int) (0 + strokeMargin), (int) (height - strokeMargin));


        Path pathBorder = new Path();
        pathBorder.setFillType(Path.FillType.EVEN_ODD);
        pathBorder.moveTo(startPointBorder.x, startPointBorder.y); // this should set the start point right

        pathBorder.lineTo(b1.x, b1.y);
        pathBorder.lineTo(c1.x, c1.y);
        pathBorder.lineTo(d1.x, d1.y);
        pathBorder.lineTo(e1.x, e1.y);
        pathBorder.lineTo(f1.x, f1.y);
        pathBorder.lineTo(g1.x, g1.y);
        pathBorder.close();

        canvas.drawPath(pathBorder, this.paintBorder);
        // Draw border>>>

        // Draw text<<<
        drawText(canvas);
        // Draw text>>>
    }


    private void drawText(Canvas canvas) {

        Rect bounds = new Rect();
        paintText.getTextBounds(this.text, 0, this.text.length(), bounds);

        int y = (int) ((int) ((canvas.getHeight() / 2) - ((paintText.descent() + paintText.ascent()) / 2)) + (this.lengthLineTriangle * 0.6));
        int x = (int) ((canvas.getWidth() / 2) - (paintText.measureText(this.text) / 2));

        this.paintText.setTextSize((float) ((canvas.getWidth() * canvas.getHeight()) * 0.00035));

        canvas.drawText(this.text, x, y, this.paintText);
    }

    public Observable<Boolean> isSetTextAnimationFinish() {
        return isSetTextAnimationEnd.hide()
                .filter(Functions.equalsWith(true));
    }

    private Runnable characterAdder = new Runnable() {
        @Override
        public void run() {
            SpecialTextView.this.text = mText.subSequence(0, mIndex++).toString();
            invalidate();
            if(mIndex <= mText.length()) {
                mHandler.postDelayed(characterAdder, mDelay);
                isSetTextAnimationEnd.onNext(false);
            } else {
                isSetTextAnimationEnd.onNext(true);
//                Log.d("TEST_GAME", "SpecialTextView -> animateText FINISH");

            }
        }
    };

    public void animateText(CharSequence text) {
//        Log.d("TEST_GAME", "SpecialTextView -> animateText");

        mText = text;
        mIndex = 0;

        clearText();

        mHandler.removeCallbacks(characterAdder);
        mHandler.postDelayed(characterAdder, mDelay);
    }

    private void clearText(){
        SpecialTextView.this.text = "";
        invalidate();
    }

    public void setCharacterDelay(long millis) {
        mDelay = millis;
    }
}
