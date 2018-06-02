package com.waterflower.drawline;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.util.Vector;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements ViewTreeObserver.OnGlobalLayoutListener {

    // component
    @BindView(R.id.textX1) EditText textX1;
    @BindView(R.id.textY1) EditText textY1;
    @BindView(R.id.textX2) EditText textX2;
    @BindView(R.id.textY2) EditText textY2;
    @BindView(R.id.btnDraw) Button btnDraw;
    @BindView(R.id.btnClear) Button btnClear;
    @BindView(R.id.imageView) ImageView imageView;

    // drawer
    Bitmap bitmap;
    Bitmap output;
    Canvas canvas;
    Paint paint;
    int drawerWidth, drawerHeight;
    static int draw_only_this_idx = -1;
    static int[] drawSizes;

    int prev_x, prev_y;//valur for [0. 10]
    int cur_x, cur_y;
    int convert_pos_x, convert_pos_y;
    boolean first_click = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        // get dynamic size imageView
        imageView.getViewTreeObserver().addOnGlobalLayoutListener(this);

        Bitmap emptyBmap = Bitmap.createBitmap(250, 200, Bitmap.Config.ARGB_8888);

        int width =  emptyBmap.getWidth();
        int height = emptyBmap.getHeight();
        Bitmap charty = Bitmap.createBitmap(width , height , Bitmap.Config.ARGB_8888);

        charty = quicky_XY(emptyBmap);

        imageView.setImageBitmap(charty);
    }

    public Bitmap quicky_XY(Bitmap emptyBmap) {
        // xode to get bitmap onto screen
//        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        output = Bitmap.createBitmap(emptyBmap.getWidth(), emptyBmap.getHeight(), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(output);

        final int color = 0xff0B0B61;
        final Paint paint = new Paint();
//        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final Rect rect = new Rect(0, 0, imageView.getHeight(), imageView.getWidth());
        final RectF rectF = new RectF(rect);
        final float roundPx = 12;

        // get the little rounded cornered outside
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);

        // ---- NOw just draw on this bitmap


        // Set the labels info manually
        String[] cur_elt_array = new String[4];

        cur_elt_array[0]="";
        cur_elt_array[1]="";
        cur_elt_array[2]="93";  // max
        cur_elt_array[3]="0";    //min

        Vector labels = new Vector();
        labels.add(cur_elt_array);

        draw_the_grid(canvas,labels);


        // se the data to be plotted and we should on our way

        Vector data_2_plot = new Vector();

        data_2_plot.add("50.2") ;
        data_2_plot.add("1.2") ;
        data_2_plot.add("9.6") ;
        data_2_plot.add("83.2") ;
        data_2_plot.add("44.2") ;

        canvas.drawBitmap(output, rect, rect, paint);

        return output;

    }



    private void draw_the_grid(Canvas this_g,  Vector these_labels) {
        double rounded_max = 0.0;
        double rounded_min = 0.0;
        double rounded_max_temp;
        Object curElt;
        String[] cur_elt_array;
        int left_margin_d, right_margin_d;

        if( draw_only_this_idx == -1)
            curElt = these_labels.elementAt(0);  // default  it to 1st one if non set
        else
            curElt = these_labels.elementAt(draw_only_this_idx);  // now just the 1st elt

        cur_elt_array = (String[])curElt;

        rounded_max = get_ceiling_or_floor (Double.parseDouble(cur_elt_array[2]) , true);
        rounded_min = get_ceiling_or_floor (Double.parseDouble(cur_elt_array[3]) , false);

        // ok so now we have the max value of the set just get a cool ceiling and we go on
        final Paint paint = new Paint();
        paint.setTextSize(15);

        int p_height = imageView.getHeight();
        int p_width = imageView.getWidth();
        int[] tmp_draw_sizes = {0, 0 , p_width - 2, p_height - 2};
        drawSizes = tmp_draw_sizes; //keep it for later processing

        //with the mzrgins worked out draw the plotting grid
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE );

        // Android does by coords
        this_g.drawRect(drawSizes[0], drawSizes[1],drawSizes[0]+
                drawSizes[2], drawSizes[1]+ drawSizes[3] , paint);

        paint.setColor(Color.BLACK);

        // finally draw the grid

        paint.setStyle(Paint.Style.STROKE);
        this_g.drawRect(drawSizes[0], drawSizes[1],drawSizes[0]+
                drawSizes[2], drawSizes[1]+ drawSizes[3] , paint);

        for(int i=1; i < 20 ; i++)
        {
            if(i == 10) paint.setStrokeWidth(4);
            else paint.setStrokeWidth(1);
            this_g.drawLine(drawSizes[0], drawSizes[1] +
                            (i * drawSizes[3] / 20), drawSizes[0] + drawSizes[2],
                    drawSizes[1] + (i * drawSizes[3] / 20), paint);
            this_g.drawLine(drawSizes[0]+ (i * drawSizes[2] / 20),
                    drawSizes[1], drawSizes[0] + (i * drawSizes[2] / 20),
                    drawSizes[1] + drawSizes[3], paint);
        }

    }

    private void drowPositionLine(Canvas this_g, int x1, int y1) {


        final Paint paint = new Paint();
        paint.setColor(Color.GRAY);

        Point cur_point = new Point();
        cur_point.set(0,0);

        convert_pos_x = (int)(x1 * imageView.getWidth() / 20.0 - 2);
        convert_pos_y = (int)(y1 * imageView.getHeight() / 20.0 - 2);

        if(first_click) {

            prev_x = (int)(imageView.getWidth() / 2.0) + convert_pos_x;
            prev_y = (int)(imageView.getHeight() / 2.0) - convert_pos_y;
            cur_x = prev_x;
            cur_y = prev_y;
            first_click = false;
        } else {

            cur_x = (int)(imageView.getWidth() / 2.0) + convert_pos_x;
            cur_y = (int)(imageView.getHeight() / 2.0) - convert_pos_y;
        }
        paint.setColor(Color.BLUE);
        this_g.drawCircle(cur_x, cur_y, 5, paint);
        paint.setColor(Color.CYAN);
        this_g.drawCircle(prev_x, prev_y, 5, paint);
        prev_x = cur_x;
        prev_y = cur_y;
    }

    public static double get_ceiling_or_floor(double this_val ,  boolean is_max  )
    {
        double this_min_tmp;
        int  this_sign;
        int  this_10_factor=0;
        double this_rounded;

        if (this_val == 0.0)
        {
            this_rounded = 0.0;
            return this_rounded;
        }

        this_min_tmp = Math.abs(this_val);

        if (this_min_tmp >= 1.0 && this_min_tmp < 10.0)
            this_10_factor = 1;
        else if (this_min_tmp >= 10.0 && this_min_tmp < 100.0)
            this_10_factor = 10;
        else if (this_min_tmp >= 100.0 && this_min_tmp < 1000.0)
            this_10_factor = 100;
        else if (this_min_tmp >= 1000.0 && this_min_tmp < 10000.0)
            this_10_factor = 1000;
        else if (this_min_tmp >= 10000.0 && this_min_tmp < 100000.0)
            this_10_factor = 10000;
        //'cover when min is pos and neg
        if (is_max)
        {
            if (this_val > 0.0)
                this_sign = 1;
            else
                this_sign = -1;

        }
        else
        {
            if (this_val > 0.0)
                this_sign = -1;
            else
                this_sign = 1;

        }

        if (this_min_tmp > 1)
            this_rounded = (double)(((int)(this_min_tmp / this_10_factor) + this_sign) * this_10_factor);
        else
        {
            this_rounded = (int)(this_min_tmp * 100.0);
            //' cover same as above bfir number up to .001 less than tha it will skip
            if (this_rounded >= 1 && this_rounded < 9)
                this_10_factor = 1;
            else if (this_rounded >= 10 && this_rounded < 99)
                this_10_factor = 10;
            else if (this_rounded >= 100 && this_rounded < 999)
                this_10_factor = 100;

            this_rounded = (double)(((int)((this_rounded) / this_10_factor) + this_sign) * this_10_factor);
            this_rounded = (int)(this_rounded) / 100.0;

        }

        if (this_val < 0)
            this_rounded = -this_rounded;

        return  this_rounded;

    } // --- end of get_ceiling_or_floor ---

    // need the width of the labels
    private static int getCurTextLengthInPixels(Paint this_paint, String this_text) {
        Paint.FontMetrics tp = this_paint.getFontMetrics();
        Rect rect = new Rect();
        this_paint.getTextBounds(this_text, 0, this_text.length(), rect);
        return rect.width();
    } // --- end of getCurTextLengthInPixels  ---


    @Override
    public void onGlobalLayout() {
        imageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

        drawerWidth = imageView.getWidth();
        drawerHeight = imageView.getHeight();

        resetDrawer();
    }

    private void resetDrawer() {
        bitmap = Bitmap.createBitmap(drawerWidth, drawerHeight, Bitmap.Config.ARGB_8888);
        imageView.setImageBitmap(quicky_XY(bitmap));

    }

    @OnClick(R.id.btnDraw)
    void btnDrawClicked() {
        if (textX1.getText().toString().trim().length() == 0) {
            textX1.setError("Cannot empty");
            return;
        }
        if (textY1.getText().toString().trim().length() == 0) {
            textY1.setError("Cannot empty");
            return;
        }

        int x1 = Integer.parseInt(textX1.getText().toString());
        int y1 = Integer.parseInt(textY1.getText().toString());

        drowPositionLine(canvas, x1, y1);
        imageView.postInvalidate();
    }
   @OnClick(R.id.btnClear)
    void btnClearClicked() {
        //resetDrawer();
    }
}
