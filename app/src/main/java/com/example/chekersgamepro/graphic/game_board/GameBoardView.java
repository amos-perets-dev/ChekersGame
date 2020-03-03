package com.example.chekersgamepro.graphic.game_board;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.example.chekersgamepro.data.BorderLine;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.subjects.PublishSubject;

public class GameBoardView extends CardView {


    private Paint paint = new Paint();

    private List<BorderLine> borderLines = new ArrayList<>();

    private PublishSubject<GameBoardView> gameBoardView = PublishSubject.create();

    public GameBoardView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        paint.setStyle(Paint.Style.STROKE);
    }

    public void drawBorders(List<BorderLine> borderLines, int borderWidth, int color){
        paint.setStrokeWidth(borderWidth);
        paint.setColor(color);
        this.borderLines = borderLines;

        invalidate();
    }

    @Override
    @SuppressLint("DrawAllocation")
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        gameBoardView.onNext(this);
        for (BorderLine borderLine : borderLines){
            canvas.drawLine(borderLine.getStart().x, borderLine.getStart().y, borderLine.getEnd().x, borderLine.getEnd().y, paint);
        }
    }

}
