package com.example.parkmaster;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Random;

public class GameCanvas extends View {
    float canvasHeight;
    float canvasWidth;
    Paint obstacleRed = new Paint();
    Paint parkingBlue = new Paint();
    Paint coinPaint1 = new Paint();
    Paint coinPaint2 = new Paint();
    Paint carPaint = new Paint();
    float carX = -1;
    float carY = -1;
    Paint whitePaint1 = new Paint();
    Boolean gameFinished = false;

    static ArrayList<Path> carPath = new ArrayList<>();
    static ArrayList<Float> carPathRefX = new ArrayList<>();
    static ArrayList<Float> carPathRefY = new ArrayList<>();
    static ArrayList<Float> coinX = new ArrayList<>();
    static ArrayList<Float> coinY = new ArrayList<>();
    static ArrayList<Float> obstacleX = new ArrayList<>();
    static ArrayList<Float> obstacleY = new ArrayList<>();
    Path mPath;
    float currentX;
    float currentY;
    float TOUCH_TOLERANCE = 4;

    Thread thread = new Thread() {
        @Override
        public void run() {
            try {
                for (int i = 0; i < carPathRefX.size(); i++) {
                    sleep(50);
                    if(carPathRefX.size()==0){
                        return;
                    }
                    carX = carPathRefX.get(i);
                    carY = carPathRefY.get(i);
                }
                restartGame();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    public GameCanvas(Context context) {
        super(context);
        init(null);
    }

    public GameCanvas(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public GameCanvas(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public GameCanvas(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(@Nullable AttributeSet set) {

        obstacleRed.setColor(getResources().getColor(R.color.obstacleRed));
        obstacleRed.setStyle(Paint.Style.STROKE);
        obstacleRed.setStrokeWidth(20);
        obstacleRed.isAntiAlias();

        parkingBlue.setColor(getResources().getColor(R.color.parkingBlue));
        parkingBlue.setStyle(Paint.Style.STROKE);
        parkingBlue.setStrokeWidth(20);
        parkingBlue.isAntiAlias();

        coinPaint1.setColor(getResources().getColor(R.color.coinOuterYellow));
        coinPaint1.setStyle(Paint.Style.FILL);
        coinPaint1.isAntiAlias();

        coinPaint2.setColor(getResources().getColor(R.color.coinInnerYellow));
        coinPaint2.setStyle(Paint.Style.FILL);
        coinPaint2.isAntiAlias();

        carPaint.setColor(getResources().getColor(R.color.coinYellow));
        carPaint.setStyle(Paint.Style.FILL);
        carPaint.setStrokeWidth(20);
        carPaint.isAntiAlias();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvasHeight = canvas.getHeight();
        canvasWidth = canvas.getWidth();
        if (carX == -1) {
            carX = canvasWidth / 2;
            carY = canvasHeight - 125;
        }
        canvas.drawColor(getResources().getColor(R.color.canvasBg));
        if (gameFinished) {
            restartGame();
        } else {
            drawParkSlot(canvas);
            drawStartPosition(canvas);
            if (coinX.size() == 0) {
                generateRandomCoinsAndObstacles(canvas);
            } else {
                drawCoins(canvas);
                drawObstacles(canvas);
            }
            drawCar(canvas);
            if (carPath.size() != 0) {
                canvas.drawPath(carPath.get(0), parkingBlue);
            }
        }
        postInvalidate();
    }

    private void generateRandomCoinsAndObstacles(Canvas canvas) {
        Random random = new Random();
        int coinsCount = random.nextInt(3) + 2;
        float coordinate;
        for (int i = 0; i < coinsCount; i++) {
            //adding coin coords
            coordinate = 50 + (random.nextFloat() * (canvasWidth - 100));
            coinX.add(coordinate);
            coordinate = 500 + (random.nextFloat() * (canvasHeight - 1000));
            coinY.add(coordinate);
            //adding obstacle coords
            coordinate = 50 + (random.nextFloat() * (canvasWidth - 100));
            obstacleX.add(coordinate);
            coordinate = 500 + (random.nextFloat() * (canvasHeight - 1000));
            obstacleY.add(coordinate);
            coordinate = obstacleX.get(obstacleX.size() - 1) - 100 + (random.nextFloat() * (obstacleX.get(obstacleX.size() - 1) - 200));
            obstacleX.add(coordinate);
            coordinate = obstacleY.get(obstacleY.size() - 1) - 100 + (random.nextFloat() * (obstacleY.get(obstacleY.size() - 1) - 200));
            obstacleY.add(coordinate);
        }
        drawCoins(canvas);
        drawObstacles(canvas);
    }

    private void drawObstacles(Canvas canvas) {
        for (int i = 0; i < coinX.size(); i += 2) {
            canvas.drawLine(obstacleX.get(i), obstacleY.get(i), obstacleX.get(i + 1), obstacleY.get(i + 1), obstacleRed);
        }
    }

    private void drawCoins(Canvas canvas) {
        for (int i = 0; i < coinX.size(); i++) {
            canvas.drawCircle(coinX.get(i), coinY.get(i), 40, coinPaint1);
            canvas.drawCircle(coinX.get(i), coinY.get(i), 25, coinPaint2);
        }
    }

    private void drawCar(Canvas canvas) {
        canvas.drawCircle(carX, carY, 30, carPaint);
        checkCoinCollision(carX, carY);
        checkObstacleCollision(carX, carY);
        postInvalidate();
    }

    private void checkCoinCollision(float x, float y) {
        for (int i = 0; i < coinX.size(); i++) {
            int sensitivity = 80;
            if ((Math.abs(x - coinX.get(i)) < sensitivity) && (Math.abs(y - coinY.get(i)) < sensitivity)) {
                coinX.remove(i);
                coinY.remove(i);
            }
        }
    }

    private void checkObstacleCollision(float x, float y) {
        for (int i = 0; i < obstacleX.size(); i += 2) {
            float length1x = Math.abs(x - obstacleX.get(i));
            float length2x = Math.abs(x - obstacleX.get(i + 1));
            float length1y = Math.abs(y - obstacleY.get(i));
            float length2y = Math.abs(y - obstacleY.get(i + 1));
            boolean a = (length1x + length2x) == Math.abs(obstacleX.get(i) - obstacleX.get(i + 1));
            boolean b = (length1y + length2y) == Math.abs(obstacleY.get(i) - obstacleY.get(i + 1));
            if (a && b) {
                restartGame();
            }
        }
    }

    private void drawParkSlot(Canvas canvas) {
        RectF parkSlot = new RectF((canvasWidth / 2) - 150, 50, (canvasWidth / 2) + 150, 200);
        canvas.drawRect(parkSlot, parkingBlue);
    }

    private void drawStartPosition(Canvas canvas) {
        RectF startPosition = new RectF((canvasWidth / 2) - 125, canvasHeight - 50,
                (canvasWidth / 2) + 125, canvasHeight - 200);
        canvas.drawRect(startPosition, parkingBlue);
    }

    private void restartGame() {
        carPathRefX.clear();
        carPathRefY.clear();
        carX = -1;
        carPath.clear();
        coinX.clear();
        coinY.clear();
        obstacleX.clear();
        obstacleY.clear();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                startDrawing(x, y);
                break;
            }
            case MotionEvent.ACTION_UP: {
                if (carPath.size() != 0) {
                    stopDrawing();
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (carPath.size() != 0) {
                    continueDrawing(x, y);
                }
                break;
            }
        }
        return true;
    }

    private void startDrawing(float x, float y) {
        if ((Math.abs(x - (canvasWidth / 2)) < 125) && ((Math.abs(y - (canvasHeight - 125)) < 125))) {
            mPath = new Path();
            carPath.add(mPath);
            mPath.reset();
            mPath.moveTo(x, y);
            carPathRefX.add(x);
            carPathRefY.add(y);
            currentX = x;
            currentY = y;
        }

    }

    private void stopDrawing() {
        if ((Math.abs(currentX - (canvasWidth / 2)) < 125) && ((Math.abs(currentY - 150) < 125))) {
            mPath.lineTo(currentX, currentY);
            driveCar();
        } else {
            carPath.clear();
            carPathRefX.clear();
            carPathRefY.clear();
        }
    }

    private void continueDrawing(float x, float y) {

        float dx = Math.abs(x - currentX);
        float dy = Math.abs(y - currentY);

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(currentX, currentY, x, y);
            currentX = x;
            currentY = y;
            carPathRefX.add(x);
            carPathRefY.add(y);
        }
    }

    private void driveCar() {
        thread.start();
    }


}
