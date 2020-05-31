package com.example.chekersgamepro.views.custom.button;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;

import com.example.chekersgamepro.R;
import com.example.chekersgamepro.util.DisplayUtil;
import com.example.chekersgamepro.views.custom.IBaseViewAnimationAngle;

import static android.graphics.Canvas.ALL_SAVE_FLAG;

public class ButtonCustom extends AppCompatTextView implements IBaseViewAnimationAngle {

    private final float DENSITY = getResources().getDisplayMetrics().density;
    private float WIDTH = (float) 0.8;
    private final float BORDER_WIDTH_DEFAULT = WIDTH * DENSITY;

    private static final String PROPERTY_SHAPE_NAME_1 = "CenterToTopRight";
    private static final String PROPERTY_SHAPE_NAME_2 = "TopRightToBottom";
    private static final String PROPERTY_SHAPE_NAME_3 = "BottomRightToLeft";
    private static final String PROPERTY_SHAPE_NAME_4 = "BottomLeftToTopLeft";
    private static final String PROPERTY_SHAPE_NAME_5 = "TopLeftToCenter";

    private static final int START_ANGLE_POINT = 270;

    private final Paint paintBorder;
    private final Paint paintBackground;

    private float angle;

    private int margin;
    private int strokeWidth;

    private long durationAnimation = getResources().getInteger(R.integer.view_default_draw_circle_animation_duration);

    Path path = new Path();

    private PropertyShapeDraw[] propertyShapeDraws;

    private int propertyShapeDrawArrayIndex = 0;

    private boolean isNeedDrawBackground = true;

    public ButtonCustom(Context context, AttributeSet attrs) {
        super(context, attrs);

//        path.setFillType(Path.FillType.EVEN_ODD);

        margin = (int) DisplayUtil.Companion.convertDpToPixel(getResources().getDimensionPixelSize(R.dimen.view_draw_circle_margin));

        paintBorder = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintBorder.setColor(Color.GREEN);                    // set the color
        paintBorder.setStrokeWidth(BORDER_WIDTH_DEFAULT);               // set the size
        paintBorder.setDither(true);                    // set the dither to true
        paintBorder.setStyle(Paint.Style.STROKE);       // set to STOKE
//        paintBorder.setStrokeJoin(Paint.Join.ROUND);    // set the join to round you want
        paintBorder.setStrokeCap(Paint.Cap.ROUND);      // set the paint cap to round too
//        paintBorder.setPathEffect(new CornerPathEffect(10));   // set the path effect when they join.
        paintBorder.setAntiAlias(true);

        paintBackground = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintBackground.setColor(Color.BLACK);                    // set the color
        paintBackground.setStrokeWidth(BORDER_WIDTH_DEFAULT);               // set the size
        paintBackground.setDither(true);                    // set the dither to true
        paintBackground.setStyle(Paint.Style.FILL_AND_STROKE);       // set to STOKE
//        paintBackground.setStrokeJoin(Paint.Join.ROUND);    // set the join to round you want
        paintBackground.setStrokeCap(Paint.Cap.ROUND);      // set the paint cap to round too
//        paintBackground.setPathEffect(new CornerPathEffect(10));   // set the path effect when they join.
        paintBackground.setAntiAlias(true);

        setLayerType(View.LAYER_TYPE_HARDWARE, null);
        setDrawingCacheEnabled(true);
        //Initial Angle (optional, it can be zero)
        angle = 0;

        PropertyShapeDraw propertyShapeDraw = new PropertyShapeDraw(true, PROPERTY_SHAPE_NAME_1);
        PropertyShapeDraw propertyShapeDraw2 = new PropertyShapeDraw(false, PROPERTY_SHAPE_NAME_2);
        PropertyShapeDraw propertyShapeDraw3 = new PropertyShapeDraw(false, PROPERTY_SHAPE_NAME_3);
        PropertyShapeDraw propertyShapeDraw4 = new PropertyShapeDraw(false, PROPERTY_SHAPE_NAME_4);
        PropertyShapeDraw propertyShapeDraw5 = new PropertyShapeDraw(false, PROPERTY_SHAPE_NAME_5);

        propertyShapeDraws =
                new PropertyShapeDraw[]{propertyShapeDraw, propertyShapeDraw2, propertyShapeDraw3, propertyShapeDraw4, propertyShapeDraw5};

    }

    @Override
    public boolean isAnimationViewAngle() {
        return true;
    }

    @Override
    public void setMarginDrawCircle(int dp) {
        this.margin = (int) DisplayUtil.Companion.convertDpToPixel(dp);
    }

    @Override
    public void setCircleWidth(float width) {
        paintBorder.setStrokeWidth(width * DENSITY);
    }

    @Override
    public void setCircleColor(int color) {
        this.paintBorder.setColor(color);
    }

    private int counterLineTopCenterToRight = 0;
    private int counterAngleRight = 0;
    private int counterY = 0;
    private int counterX = 0;


    @Override
    @SuppressLint({"DrawAllocation", "SetTextI18n"})
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Rect clipBounds = canvas.getClipBounds();

        int marginLeftRightBackground = (int) (clipBounds.width() * 0.01);

        // Draw background <<<
        float cornerRadius = (float) (clipBounds.height() * 0.7);
        Log.d("TEST_GAME", "cornerRadius: " + cornerRadius);
        float[] corners = {
                cornerRadius, cornerRadius,        // Top left radius in px
                cornerRadius, cornerRadius,        // Top right radius in px
                cornerRadius, cornerRadius,          // Bottom right radius in px
                cornerRadius, cornerRadius,          // Bottom left radius in px
        };

        final Path pathBackground = new Path();

        RectF rectBackground = new RectF(
                clipBounds.left + marginLeftRightBackground
                , clipBounds.top
                , clipBounds.right - marginLeftRightBackground
                , clipBounds.bottom);
        pathBackground.addRoundRect(rectBackground, corners, Path.Direction.CW);
        pathBackground.close();
        canvas.drawPath(pathBackground, paintBackground);
        // Draw background >>>


        // Draw border
        int x = clipBounds.width() / 2;
        int x1 = x + ++counterX;
        int marginStrokeBorder = (int) ((int) BORDER_WIDTH_DEFAULT + (clipBounds.height() * 0.02));
        int yOrTop = marginStrokeBorder;
        int borderDrawLineRight = (int) (clipBounds.right - (clipBounds.width() * 0.075));
        int borderDrawLineLeft = (int) (clipBounds.left + (clipBounds.width() * 0.075));

        path.moveTo(x, yOrTop);

        String property = propertyShapeDraws[propertyShapeDrawArrayIndex].getPropertyName();
        int widthLessMargin = (int) (clipBounds.width() - marginLeftRightBackground);

        switch (property) {
            case PROPERTY_SHAPE_NAME_1:
                if ((counterX + (clipBounds.width() / 2)) < borderDrawLineRight) {
                    path.lineTo(x1, yOrTop); //path top- center to right
                } else {
                    propertyShapeDrawArrayIndex++;
                    counterX = 0;
                    path.close();
                }
                break;

            case PROPERTY_SHAPE_NAME_2:
                int sumBetweenBorders = widthLessMargin - borderDrawLineRight;

                RectF rectRight = new RectF(
                        borderDrawLineRight - sumBetweenBorders
                        , yOrTop
                        , widthLessMargin
                        , clipBounds.bottom - marginStrokeBorder);
                path.arcTo(rectRight, 270, counterX);

                if (counterX == 180) {
                    counterX = 0;
                    propertyShapeDrawArrayIndex++;
                    path.close();
                }

                break;

            case PROPERTY_SHAPE_NAME_3:
                int counterToStartX = borderDrawLineRight - counterX;
                if (borderDrawLineRight - counterX > borderDrawLineLeft){
                    path.lineTo(counterToStartX, clipBounds.bottom - marginStrokeBorder);
                } else {
                    counterX = 0;
                    propertyShapeDrawArrayIndex++;
                    path.close();
                }
                break;

            case PROPERTY_SHAPE_NAME_4:

//                int sumBetweenBorders = widthLessMargin - borderDrawLineLeft;

                RectF rectLeft = new RectF(
                        marginStrokeBorder
                        , yOrTop
                        , borderDrawLineLeft * 2
                        , clipBounds.bottom - marginStrokeBorder);
                path.arcTo(rectLeft, 450, counterX);

                if (counterX == 180) {
                    counterX = 0;
                    propertyShapeDrawArrayIndex++;
                    path.close();
                }

                break;

            case PROPERTY_SHAPE_NAME_5:

                if (borderDrawLineLeft + counterX < (clipBounds.width() / 2)) {
                    path.lineTo(borderDrawLineLeft + counterX, yOrTop); //path top- center to right
                } /*else {
                    propertyShapeDrawArrayIndex++;
                    counterX = 0;
                    path.close();
                }
*/
                break;

        }


//        if ((counterX + (clipBounds.width() / 2)) < borderDrawLine) {
//            path.lineTo(x1, yOrTop); //path top- center to right
//        } else {
//            int width = (int) (clipBounds.width() - marginLeftRightBackground);
//            int sumBetweenBorders = width - borderDrawLine;
//
//
//            RectF rectF = new RectF(borderDrawLine - sumBetweenBorders, yOrTop, width, clipBounds.bottom - marginStrokeBorder);
//            path.arcTo(rectF, 270, 180);
//        }

        path.close();
        canvas.drawPath(path, paintBorder);
    }

    @Override
    public float getAngle() {
        return angle;
    }

    @Override
    public long getDrawAnimationDuration() {
        return durationAnimation;
    }

    @Override
    public void setAngle(float angle) {
        this.angle = angle;
    }

    @Override
    public void requestLayoutNow() {
        requestLayout();
    }

    @Override
    public void setDurationAnimation(long durationAnimation) {
        this.durationAnimation = durationAnimation;
    }

    private class PropertyShapeDraw {

        private Boolean isNeedDraw;
        private String propertyName;

        public PropertyShapeDraw(Boolean isNeedDraw, String propertyName) {
            this.isNeedDraw = isNeedDraw;
            this.propertyName = propertyName;
        }

        public Boolean getNeedDraw() {
            return isNeedDraw;
        }

        public PropertyShapeDraw setNeedDraw(Boolean needDraw) {
            isNeedDraw = needDraw;
            return this;
        }

        public String getPropertyName() {
            return propertyName;
        }

        public PropertyShapeDraw setPropertyName(String propertyName) {
            this.propertyName = propertyName;
            return this;
        }
    }

}
