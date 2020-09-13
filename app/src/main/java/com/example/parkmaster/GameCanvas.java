package com.example.parkmaster;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
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
    Paint parkingOrange = new Paint();
    Paint parkingGreen = new Paint();
    Paint coinPaint1 = new Paint();
    Paint coinPaint2 = new Paint();
    int coinScored = 0;
    Paint startPositionPaint = new Paint();
    float carBX = -1;
    float carBY = -1;
    float carAX = -1;
    float carAY = -1;
    float carCX = -1;
    float carCY = -1;
    Drawable carBDrawable;
    Drawable carADrawable;
    Drawable carCDrawable;
    boolean gameOver = false;
    float dx = 0;
    float dy = 0;

    static ArrayList<Path> carBPath = new ArrayList<>();
    static ArrayList<Float> carBPathRefX = new ArrayList<>();
    static ArrayList<Float> carBPathRefY = new ArrayList<>();

    static ArrayList<Path> carAPath = new ArrayList<>();
    static ArrayList<Float> carAPathRefX = new ArrayList<>();
    static ArrayList<Float> carAPathRefY = new ArrayList<>();

    static ArrayList<Path> carCPath = new ArrayList<>();
    static ArrayList<Float> carCPathRefX = new ArrayList<>();
    static ArrayList<Float> carCPathRefY = new ArrayList<>();

    int currentPath = 0;

    static ArrayList<Float> coinX = new ArrayList<>();
    static ArrayList<Float> coinY = new ArrayList<>();
    static ArrayList<Float> obstacleX = new ArrayList<>();
    static ArrayList<Float> obstacleY = new ArrayList<>();
    Path mBPath;
    Path mAPath;
    Path mCPath;
    float currentX;
    float currentY;
    float touchTolerance = 4;
    float parkYTolerance = 50;

    private GameCanvasListener listener;

    public interface GameCanvasListener {
        int onResult(String msg);
    }

    Thread thread = new Thread() {
        @Override
        public void run() {
            while (true) {
                try {
                    if (gameOver) {

                        int max = Math.max(carBPathRefX.size(), carAPathRefX.size());
                        max = Math.max(max, carCPathRefX.size());

                        for (int i = 0; i < max; i++) {
                            sleep(20);
                            if ((carBPathRefX.size() != 0) && (i < carBPathRefX.size())) {
                                carBX = carBPathRefX.get(i);
                                carBY = carBPathRefY.get(i);
//                                if (i != (carBPathRefX.size() - 1)) {
//                                    dx = carBPathRefX.get(i + 1) - carBPathRefX.get(i);
//                                    dy = carBPathRefY.get(i + 1) - carBPathRefY.get(i);
//                                }
                            }
                            if ((carAPathRefX.size() != 0) && (i < carAPathRefX.size())) {
                                carAX = carAPathRefX.get(i);
                                carAY = carAPathRefY.get(i);
                            }
                            if ((carCPathRefX.size() != 0) && (i < carCPathRefX.size())) {
                                carCX = carCPathRefX.get(i);
                                carCY = carCPathRefY.get(i);
                            }
                        }
                        if ((carBPathRefX.size() != 0) || (carAPathRefX.size() != 0) || (carCPathRefX.size() != 0)) {// this might be zero when an obstacle is hit
                            switch (coinScored) {
                                case 0: {
                                    listener.onResult("Car Parked without gaining Coins");
                                    break;
                                }
                                case 1: {
                                    listener.onResult("You have scored " + coinScored + " Coin!");
                                    break;
                                }
                                default: {
                                    listener.onResult("You have scored " + coinScored + " Coins!");
                                }
                            }
                            restartGame();
                        }
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public GameCanvas(Context context) {
        super(context);
        init();
    }

    public GameCanvas(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GameCanvas(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public GameCanvas(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {

        listener = (GameCanvasListener) getContext();

        obstacleRed.setColor(getResources().getColor(R.color.obstacleRed));
        obstacleRed.setStyle(Paint.Style.STROKE);
        obstacleRed.setStrokeWidth(20);
        obstacleRed.isAntiAlias();

        parkingBlue.setColor(getResources().getColor(R.color.parkingBlue));
        parkingBlue.setStyle(Paint.Style.STROKE);
        parkingBlue.setStrokeWidth(20);
        parkingBlue.isAntiAlias();

        parkingGreen.setColor(getResources().getColor(R.color.parkingGreen));
        parkingGreen.setStyle(Paint.Style.STROKE);
        parkingGreen.setStrokeWidth(20);
        parkingGreen.isAntiAlias();

        parkingOrange.setColor(getResources().getColor(R.color.parkingOrange));
        parkingOrange.setStyle(Paint.Style.STROKE);
        parkingOrange.setStrokeWidth(20);
        parkingOrange.isAntiAlias();

        coinPaint1.setColor(getResources().getColor(R.color.coinOuterYellow));
        coinPaint1.setStyle(Paint.Style.FILL);
        coinPaint1.isAntiAlias();

        coinPaint2.setColor(getResources().getColor(R.color.coinInnerYellow));
        coinPaint2.setStyle(Paint.Style.FILL);
        coinPaint2.isAntiAlias();

        startPositionPaint.setColor(getResources().getColor(R.color.startPosition));
        startPositionPaint.setStyle(Paint.Style.FILL);
        startPositionPaint.isAntiAlias();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvasHeight = canvas.getHeight();
        canvasWidth = canvas.getWidth();
        if (carBX == -1) {
            setCarPositions();
        }
        canvas.drawColor(getResources().getColor(R.color.canvasBg));
        drawParkSlot(canvas);
        drawStartPosition(canvas);
        if (obstacleX.size() == 0) {
            generateRandomCoinsAndObstacles(canvas);
        } else {
            drawCoins(canvas);
            drawObstacles(canvas);
        }
        drawCar(canvas);
        if (carBPath.size() != 0) {
            canvas.drawPath(carBPath.get(0), parkingBlue);
        }
        if (carAPath.size() != 0) {
            canvas.drawPath(carAPath.get(0), parkingGreen);
        }
        if (carCPath.size() != 0) {
            canvas.drawPath(carCPath.get(0), parkingOrange);
        }
        postInvalidate();
    }

    private void setCarPositions() {
        carBX = canvasWidth / 2;
        carBY = canvasHeight - 150;
        carAX = 125;
        carCX = canvasWidth - carAX;
        carAY = carBY;
        carCY = carBY;
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
        for (int i = 0; i < obstacleX.size(); i += 2) {
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
        float angle = (float) ((Math.atan(dy / dx) * 180) / 3.14);
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        carBDrawable = getResources().getDrawable(R.drawable.ic_carsvg_blue, null);
        carBDrawable.setBounds((int) carBX - 100, (int) carBY - 125, (int) carBX + 100, (int) carBY + 125);
        carBDrawable.draw(canvas);

        carADrawable = getResources().getDrawable(R.drawable.ic_carsvg_green, null);
        carADrawable.setBounds((int) carAX - 100, (int) carAY - 125, (int) carAX + 100, (int) carAY + 125);
        carADrawable.draw(canvas);

        carCDrawable = getResources().getDrawable(R.drawable.ic_carsvg_orange, null);
        carCDrawable.setBounds((int) carCX - 100, (int) carCY - 125, (int) carCX + 100, (int) carCY + 125);
        carCDrawable.draw(canvas);
        //canvas.drawCircle(carX, carY, 30, carPaint);
        checkCoinCollision();
        checkObstacleCollision(carAX, carAY, carBX, carBY, carCX, carCY);
        checkCarCollision(carAX, carAY, carBX, carBY);
        checkCarCollision(carCX, carCY, carBX, carBY);
        checkCarCollision(carAX, carAY, carCX, carCY);
        postInvalidate();
    }

    private void checkCarCollision(float car1X, float car1Y, float car2X, float car2Y) {
        int carCollisionTolerance = 150;
        if ((Math.abs(car1X - car2X) < carCollisionTolerance) && (Math.abs(car1Y - car2Y) < carCollisionTolerance)) {
            listener.onResult("Oops! Your cars collided with each other !");
            restartGame();
        }

    }

    private void checkCoinCollision() {
        for (int i = 0; i < coinX.size(); i++) {
            int sensitivity = 100;
            boolean a = ((Math.abs(carAX - coinX.get(i)) < sensitivity) && (Math.abs(carAY - coinY.get(i)) < sensitivity));
            boolean b = ((Math.abs(carBX - coinX.get(i)) < sensitivity) && (Math.abs(carBY - coinY.get(i)) < sensitivity));
            boolean c = ((Math.abs(carCX - coinX.get(i)) < sensitivity) && (Math.abs(carCY - coinY.get(i)) < sensitivity));
            if (a || b || c) {
                coinX.remove(i);
                coinY.remove(i);
                coinScored++;
            }
        }
    }

    private void checkObstacleCollision(float x1, float y1, float x2, float y2, float x3, float y3) {
        for (int i = 0; i < obstacleX.size(); i += 2) {
            boolean a = isObstacleNear(i, x1, y1);
            boolean b = isObstacleNear(i, x2, y2);
            boolean c = isObstacleNear(i, x3, y3);
            if (a || b || c) {
                listener.onResult("Oops! You hit an obstacle !");
                restartGame();
            }
        }
    }

    private boolean isObstacleNear(int i, float x, float y) {
        float length1x = Math.abs(x - obstacleX.get(i));
        float length2x = Math.abs(x - obstacleX.get(i + 1));
        float length1y = Math.abs(y - obstacleY.get(i));
        float length2y = Math.abs(y - obstacleY.get(i + 1));
        float collisionTolerance = 20;
        float lengthA = (length1x + length2x) - Math.abs(obstacleX.get(i) - obstacleX.get(i + 1));
        float lengthB = (length1y + length2y) - Math.abs(obstacleY.get(i) - obstacleY.get(i + 1));
        boolean a = (Math.abs(lengthA) < collisionTolerance);
        boolean b = (Math.abs(lengthB) < collisionTolerance);
        return (a && b);
    }

    private void drawParkSlot(Canvas canvas) {
        RectF parkSlot = new RectF((canvasWidth / 2) - 150, 50, (canvasWidth / 2) + 150, 200);
        canvas.drawRect(parkSlot, parkingBlue);
        parkSlot = new RectF(50, 50, 350, 200);
        canvas.drawRect(parkSlot, parkingGreen);
        parkSlot = new RectF(canvasWidth - 50, 50, canvasWidth - 350, 200);
        canvas.drawRect(parkSlot, parkingOrange);
    }

    private void drawStartPosition(Canvas canvas) {
        RectF startPosition = new RectF((canvasWidth / 2) - 125, canvasHeight - 50,
                (canvasWidth / 2) + 125, canvasHeight - 200);
        canvas.drawRect(startPosition, startPositionPaint);
        startPosition = new RectF(20, canvasHeight - 50, 270, canvasHeight - 200);
        canvas.drawRect(startPosition, startPositionPaint);
        startPosition = new RectF(canvasWidth - 20, canvasHeight - 50, canvasWidth - 270, canvasHeight - 200);
        canvas.drawRect(startPosition, startPositionPaint);
    }

    private void restartGame() {
        restartCar(carAPathRefX, carAPath, carAPathRefY);
        restartCar(carBPathRefX, carBPath, carBPathRefY);
        restartCar(carCPathRefX, carCPath, carCPathRefY);
        carBX = -1;
        coinX.clear();
        coinY.clear();
        obstacleX.clear();
        obstacleY.clear();
        coinScored = 0;
        gameOver = false;
        dx = 0;
        dy = 0;
    }

    private void restartCar(ArrayList<Float> carPathRefX, ArrayList<Path> carPath, ArrayList<Float> carPathRefY) {
        carPathRefX.clear();
        carPathRefY.clear();
        carPath.clear();
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
                switch (currentPath) {
                    case 0: {
                        if (carAPath.size() != 0) {
                            stopDrawing();
                        }
                        break;
                    }
                    case 1: {
                        if (carBPath.size() != 0) {
                            stopDrawing();
                        }
                        break;
                    }
                    case 2: {
                        if (carCPath.size() != 0) {
                            stopDrawing();
                        }
                        break;
                    }
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                switch (currentPath) {
                    case 0: {
                        float horizParkCentre = 125;
                        if ((carAPath.size() != 0) &&
                                !((Math.abs(currentX - horizParkCentre) < 125) && ((Math.abs(currentY - 150) < parkYTolerance)))) {
                            continueDrawing(x, y);
                        }
                        break;
                    }
                    case 1: {
                        float horizParkCentre = canvasWidth / 2;
                        if ((carBPath.size() != 0) &&
                                !((Math.abs(currentX - horizParkCentre) < 125) && ((Math.abs(currentY - 150) < parkYTolerance)))) {
                            continueDrawing(x, y);
                        }
                        break;
                    }
                    case 2: {
                        float horizParkCentre = canvasWidth - 125;
                        if ((carCPath.size() != 0) &&
                                !((Math.abs(currentX - horizParkCentre) < 125) && ((Math.abs(currentY - 150) < parkYTolerance)))) {
                            continueDrawing(x, y);
                        }
                        break;
                    }
                }
                break;
            }
        }
        return true;
    }

    private void startDrawing(float x, float y) {
        if ((Math.abs(x - (canvasWidth / 2)) < 125) && ((Math.abs(y - (canvasHeight - 125)) < 125))) {
            mBPath = new Path();
            startDrawingCarN(mBPath, carBPath, carBPathRefX, carBPathRefY, 1, x, y);
        } else if ((Math.abs(x - 125) < 125) && ((Math.abs(y - (canvasHeight - 125)) < 125))) {
            mAPath = new Path();
            startDrawingCarN(mAPath, carAPath, carAPathRefX, carAPathRefY, 0, x, y);
        } else if ((Math.abs(x - (canvasWidth - 125)) < 125) && ((Math.abs(y - (canvasHeight - 125)) < 125))) {
            mCPath = new Path();
            startDrawingCarN(mCPath, carCPath, carCPathRefX, carCPathRefY, 2, x, y);
        }
    }

    private void startDrawingCarN(Path path, ArrayList<Path> carPath, ArrayList<Float> carPathRefX, ArrayList<Float> carPathRefY, int i, float x, float y) {
        carPath.add(path);
        path.reset();
        path.moveTo(x, y);
        carPathRefX.add(x);
        carPathRefY.add(y);
        currentPath = i;
        currentX = x;
        currentY = y;
    }

    private void stopDrawing() {

        switch (currentPath) {
            case 0: {
                stopCarNPath(mAPath, carAPathRefX, carAPathRefY, carAPath, 125);
                break;
            }
            case 1: {
                stopCarNPath(mBPath, carBPathRefX, carBPathRefY, carBPath, (canvasWidth / 2));
                break;
            }
            case 2: {
                stopCarNPath(mCPath, carCPathRefX, carCPathRefY, carCPath, canvasWidth - 125);
                break;
            }
        }

    }

    private void stopCarNPath(Path mPath, ArrayList<Float> carPathRefX,
                              ArrayList<Float> carPathRefY, ArrayList<Path> carPath, float horizParkCentre) {
        if ((Math.abs(currentX - horizParkCentre) < 125) && ((Math.abs(currentY - 150) < parkYTolerance))) {
            mPath.lineTo(currentX, currentY);
            if ((carAPathRefX.size() != 0) && (carBPathRefX.size() != 0) && (carCPathRefX.size() != 0)) {
                driveCar();
            }
        } else {
            carPath.clear();
            carPathRefX.clear();
            carPathRefY.clear();
        }
    }

    private void continueDrawing(float x, float y) {

        float dx = Math.abs(x - currentX);
        float dy = Math.abs(y - currentY);

        if (dx >= touchTolerance || dy >= touchTolerance) {
            currentX = x;
            currentY = y;
            switch (currentPath) {
                case 0: {
                    continueCarNPath(mAPath, x, y, carAPathRefX, carAPathRefY);
                    break;
                }
                case 1: {
                    continueCarNPath(mBPath, x, y, carBPathRefX, carBPathRefY);
                    break;
                }
                case 2: {
                    continueCarNPath(mCPath, x, y, carCPathRefX, carCPathRefY);
                    break;
                }
            }

        }
    }

    private void continueCarNPath(Path mPath, float x, float y, ArrayList<Float> carPathRefX, ArrayList<Float> carPathRefY) {
        mPath.lineTo(x, y);
        carPathRefX.add(x);
        carPathRefY.add(y);
    }

    private void driveCar() {
        if (!thread.isAlive()) {
            thread.start();
        }
        gameOver = true;
    }


}
