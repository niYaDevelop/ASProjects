package space.yakimov.firstapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;

public class MapView extends View {
    private Paint p = new Paint();
    private Paint blue = new Paint();
    private Paint green = new Paint();
    private Paint yellow = new Paint();
    private Paint red = new Paint();
    private Bitmap blueIm;
    private Bitmap whiteIm;
    private Bitmap greenIm;
    private Bitmap redIm;
    private Bitmap yellowIm;
    Typeface font;

    private Paint pText = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint blueText = new Paint(Paint.ANTI_ALIAS_FLAG);


    private int size;
    private Tile[][] tiles;
    private int side;
    private int width;

    public MapView(Context context, AttributeSet set){
        super(context,set);
        font = Typeface.createFromAsset(context.getAssets(), "fonts/comic.ttf");
    }

    public void init (int n, int width, String matrixArray[]) {

        this.size = n;
        this.width = width;
        side = width / n;

        p.setStyle(Paint.Style.FILL);
        p.setColor(getResources().getColor(R.color.clouds));

        pText.setColor(getResources().getColor(R.color.midnightBlue));
        pText.setTextAlign(Paint.Align.CENTER);
        pText.setTextSize(side * 75 / 100);
        pText.setTypeface(font);

        blue.setStyle(Paint.Style.FILL);
        blue.setColor(getResources().getColor(R.color.peterRiver));

        blueText.setColor(getResources().getColor(R.color.clouds));
        blueText.setTextAlign(Paint.Align.CENTER);
        blueText.setTextSize(side * 75 / 100);
        blueText.setTypeface(font);

        green.setStyle(Paint.Style.FILL);
        green.setColor(getResources().getColor(R.color.emerald));

        yellow.setStyle(Paint.Style.FILL);
        yellow.setColor(getResources().getColor(R.color.sunFlower));

        red.setStyle(Paint.Style.FILL);
        red.setColor(getResources().getColor(R.color.alizarin));


        blueIm = BitmapFactory.decodeResource(getResources(), R.drawable.blue);
        whiteIm = BitmapFactory.decodeResource(getResources(), R.drawable.white);
        redIm = BitmapFactory.decodeResource(getResources(), R.drawable.red);
        greenIm = BitmapFactory.decodeResource(getResources(), R.drawable.green);
        yellowIm = BitmapFactory.decodeResource(getResources(), R.drawable.yellow);


        tiles = new Tile[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                tiles[i][j] = new Tile(matrixArray[i].charAt(j));
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        // вызываем метод onMeasure класса ImageButton, чтобы расcчитать размеры
        // кнопки стандартным образом
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // сейчас наша кнопка имеет такие же размеры как если бы
        // она была экземпляром класса ImageButton

        // начинаем добавлять новую логику расчета размера

        // получаем рассчитанные размеры кнопки
        final int heit = getMeasuredHeight();	// высота
        final int wit = width;	// ширина

        // теперь задаем новый размер
        // ширину оставляем такую же как у стандартной кнопки
        // высоту выбираем как максимум между стандартной высотой и шириной
        setMeasuredDimension(wit, wit);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        try {

            int sx = 0, sy = 0;
            int x = 0, y = 0;
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    if (EasyLevel.globalCheck[i][j] == 0) {
                        x += side;
                        continue;
                    }
                    switch (EasyLevel.checkArray[i][j]) {
                        case 0:
                            canvas.drawBitmap(whiteIm, null, new RectF(x, y, x + side, y + side), null);
                            //  canvas.drawRoundRect(new RectF(x, y, x + side, y + side), side / 5, side / 5, p);
                            canvas.drawText(String.valueOf(tiles[i][j].letter), x + side / 2, y + 5 * side / 8, pText);
                            break;
                        case 1:
                            canvas.drawBitmap(blueIm, null, new RectF(x, y, x + side, y + side), null);
                            // canvas.drawRoundRect(new RectF(x, y, x + side, y + side), side / 5, side / 5, blue);
                            canvas.drawText(String.valueOf(tiles[i][j].letter), x + side / 2, y + 5 * side / 8 + side / 30, blueText);
                            break;
                        case 2:
                            canvas.drawBitmap(greenIm, null, new RectF(x, y, x + side, y + side), null);
                            // canvas.drawRoundRect(new RectF(x, y, x + side, y + side), side / 5, side / 5, green);
                            canvas.drawText(String.valueOf(tiles[i][j].letter), x + side / 2, y + 5 * side / 8 + side / 30, blueText);
                            break;
                        case 3:
                            canvas.drawBitmap(yellowIm, null, new RectF(x, y, x + side, y + side), null);
                            //canvas.drawRoundRect(new RectF(x, y, x + side, y + side), side / 5, side / 5, yellow);
                            canvas.drawText(String.valueOf(tiles[i][j].letter), x + side / 2, y + 5 * side / 8 + side / 30, blueText);
                            break;

                        case 4:
                            canvas.drawBitmap(redIm, null, new RectF(x, y, x + side, y + side), null);
                            //canvas.drawRoundRect(new RectF(x, y, x + side, y + side), side / 5, side / 5, red);
                            canvas.drawText(String.valueOf(tiles[i][j].letter), x + side / 2, y + 5 * side / 8 + side / 30, blueText);
                            break;
                    }
                    x += side;
                }
                y += side;
                x = sx;
            }
        } catch (Exception e) {
        }
    }
}
