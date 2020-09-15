package com.ypw.code.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * 原库地址：https://github.com/lihangleo2/ShadowLayout
 * 阴影控件
 */
public class ShadowLayout extends FrameLayout {

    private int mBgColor;
    private int mShadowColor;
    private float mShadowLength;
    private float mRadius;
    private float mShadowOffsetX;
    private float mShadowOffsetY;
    private boolean mShowLeftShadow;
    private boolean mShowRightShadow;
    private boolean mShowTopShadow;
    private boolean mShowBottomShadow;

    private Paint mShadowPaint;
    private Paint mBgPaint;

    private int mLeftPadding;
    private int mTopPadding;
    private int mRightPadding;
    private int mBottomPadding;

    //阴影布局子空间区域
    private RectF rectf = new RectF();

    public ShadowLayout(Context context) {
        this(context, null);
    }

    public ShadowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.ShadowLayout);
        if (array != null) {
            mShowLeftShadow = array.getBoolean(R.styleable.ShadowLayout_shadow_showLeftShadow, true);
            mShowRightShadow = array.getBoolean(R.styleable.ShadowLayout_shadow_showRightShadow, true);
            mShowBottomShadow = array.getBoolean(R.styleable.ShadowLayout_shadow_showBottomShadow, true);
            mShowTopShadow = array.getBoolean(R.styleable.ShadowLayout_shadow_showTopShadow, true);
            mRadius = array.getDimension(R.styleable.ShadowLayout_shadow_radius, 0);
            mShadowLength = array.getDimension(R.styleable.ShadowLayout_shadow_limit, 10);
            mShadowOffsetX = array.getDimension(R.styleable.ShadowLayout_shadow_shadowOffsetX, 0);
            mShadowOffsetY = array.getDimension(R.styleable.ShadowLayout_shadow_shadowOffsetY, 0);
            mShadowColor = array.getColor(R.styleable.ShadowLayout_shadow_color, 0x2a000000);
            mBgColor = array.getColor(R.styleable.ShadowLayout_shadow_bgColor, 0xffffffff);
            array.recycle();
        }
        onInit();
        setWillNotDraw(false);
    }

    private void onInit() {
        mShadowPaint = new Paint();
        mShadowPaint.setAntiAlias(true);
        mShadowPaint.setStyle(Paint.Style.FILL);

        mBgPaint = new Paint();
        mBgPaint.setAntiAlias(true);
        mBgPaint.setStyle(Paint.Style.FILL);
        mBgPaint.setColor(mBgColor);
        computePadding();
    }

    public void computePadding() {
        int xPadding = (int) (mShadowLength + mShadowOffsetX);
        int yPadding = (int) (mShadowLength + mShadowOffsetY);

        if (mShowLeftShadow) {
            mLeftPadding = xPadding;
        } else {
            mLeftPadding = 0;
        }

        if (mShowRightShadow) {
            mRightPadding = xPadding;
        } else {
            mRightPadding = 0;
        }

        if (mShowTopShadow) {
            mTopPadding = yPadding;
        } else {
            mTopPadding = 0;
        }

        if (mShowBottomShadow) {
            mBottomPadding = yPadding;
        } else {
            mBottomPadding = 0;
        }

        setPadding(mLeftPadding, mTopPadding, mRightPadding, mBottomPadding);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (getChildCount() < 1) {
            return;
        }

        // ======================================================
        // 通过 MeasureSpec.getMode 获取模式存在一个问题:
        // 当获取 MeasureSpec.getMode == MeasureSpec.UNSPECIFIED 时,
        // 即使 xml 宽高设置的是具体值, MeasureSpec.getSize 获取到也还是推荐的值.
        // ======================================================
        // 下面的代码就是为了解决这种情况:
        // 为了避免 MeasureSpec.UNSPECIFIED 模式下获取不到 xml 设置的具体宽高值, 直接从 LayoutParams 中获取
        int width = getLayoutParams().width;
        int height = getLayoutParams().height;

        // 计算剩余空间
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int useWidth = (width > 0 ? width : widthSize) - mLeftPadding - mRightPadding;
        int childWidthSpec = MeasureSpec.makeMeasureSpec(useWidth, MeasureSpec.EXACTLY);

        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int useHeight = (height > 0 ? height : heightSize) - mTopPadding - mBottomPadding;
        int childHeightSpec = MeasureSpec.makeMeasureSpec(useHeight, MeasureSpec.EXACTLY);

        View child = getChildAt(0);
        measureChild(child, childWidthSpec, childHeightSpec);

        // MATCH_PARENT
        if (width == ViewGroup.LayoutParams.MATCH_PARENT) {
            width = widthSize;
        }
        if (height == ViewGroup.LayoutParams.MATCH_PARENT) {
            height = heightSize;
        }

        // WRAP_CONTENT
        MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
        if (width == ViewGroup.LayoutParams.WRAP_CONTENT) {
            width = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin + mLeftPadding + mRightPadding;
        }
        if (height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            height = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin + mTopPadding + mBottomPadding;
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        rectf.left = mLeftPadding;
        rectf.top = mTopPadding;
        rectf.right = getWidth() - mRightPadding;
        rectf.bottom = getHeight() - mBottomPadding;
        float tempRadius = Math.min(rectf.height() / 2, mRadius);
        canvas.drawRoundRect(rectf, tempRadius, tempRadius, mBgPaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0) {
            setBackgroundCompat(w, h);
        }
    }

    private void setBackgroundCompat(int w, int h) {
        Bitmap bitmap = createShadowBitmap(w, h, mRadius, mShadowLength, mShadowOffsetX, mShadowOffsetY, mShadowColor, Color.WHITE);
        setBackground(new BitmapDrawable(null, bitmap));
    }

    private Bitmap createShadowBitmap(int shadowWidth, int shadowHeight, float cornerRadius, float shadowLength, float dx, float dy, int shadowColor, int fillColor) {
        //优化阴影bitmap大小,将尺寸缩小至原来的1/4。
        dx = dx / 4;
        dy = dy / 4;
        shadowWidth = shadowWidth / 4;
        shadowHeight = shadowHeight / 4;
        cornerRadius = cornerRadius / 4;
        shadowLength = shadowLength / 4;

        Bitmap output = Bitmap.createBitmap(shadowWidth, shadowHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        RectF shadowRect = new RectF(shadowLength, shadowLength, shadowWidth - shadowLength, shadowHeight - shadowLength);

        shadowRect.top += dy;
        shadowRect.bottom -= dy;
        shadowRect.left += dx;
        shadowRect.right -= dx;

        mShadowPaint.setColor(fillColor);

        // * 0.8 避免边缘出现明显分界线
        mShadowPaint.setShadowLayer(shadowLength * 0.8f, dx, dy, shadowColor);

        canvas.drawRoundRect(shadowRect, cornerRadius, cornerRadius, mShadowPaint);
        return output;
    }

}

