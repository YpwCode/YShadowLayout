package com.ypw.code.view

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout

/**
 * *************************
 * Ypw
 * ypwcode@163.com 
 * 2020/9/30 9:54 AM
 * -------------------------
 * 参考: https://github.com/lihangleo2/ShadowLayout
 * 阴影控件
 * *************************
 */
class ShadowLayout(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {

    private val xfermode: Xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)

    private var mBgColor = 0
    private var mShadowColor = 0
    private var mShadowLength = 0f
    private var mRadius = 0f
    private var mShadowOffsetX = 0f
    private var mShadowOffsetY = 0f
    private var mShowLeftShadow = false
    private var mShowRightShadow = false
    private var mShowTopShadow = false
    private var mShowBottomShadow = false
    private var mShadowMarginLeft = 0f
    private var mShadowMarginRight = 0f
    private var mShadowMarginTop = 0f
    private var mShadowMarginBottom = 0f

    private var mLeftTopRadius = 0f
    private var mLeftBottomRadius = 0f
    private var mRightTopRadius = 0f
    private var mRightBottomRadius = 0f

    private var mUseGradient = false
    private var mGradientStartColor = 0
    private var mGradientEndColor = 0
    private var mGradientAngle = 0

    private var mCircle = false

    // 阴影布局子空间区域
    private val mBgRect = RectF()
    private val mShadowRect = RectF()

    private val mShadowPaint by lazy {
        Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = mShadowColor
        }
    }

    private val mBgPaint by lazy {
        Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = mBgColor
        }
    }

    private val mTranPaint by lazy {
        Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = Color.TRANSPARENT
        }
    }

    private val mShaderGradient by lazy {
        val x0: Float
        val y0: Float
        val x1: Float
        val y1: Float
        when (mGradientAngle) {
            0 -> {
                x0 = 0f
                y0 = 0f
                x1 = width.toFloat()
                y1 = 0f
            }
            45 -> {
                x0 = 0f
                y0 = height.toFloat()
                x1 = width.toFloat()
                y1 = 0f
            }
            90 -> {
                x0 = 0f
                y0 = height.toFloat()
                x1 = 0f
                y1 = 0f
            }
            135 -> {
                x0 = width.toFloat()
                y0 = height.toFloat()
                x1 = 0f
                y1 = 0f
            }
            180 -> {
                x0 = width.toFloat()
                y0 = 0f
                x1 = 0f
                y1 = 0f
            }
            225 -> {
                x0 = width.toFloat()
                y0 = 0f
                x1 = 0f
                y1 = height.toFloat()
            }
            270 -> {
                x0 = 0f
                y0 = 0f
                x1 = 0f
                y1 = height.toFloat()
            }
            315 -> {
                x0 = 0f
                y0 = 0f
                x1 = width.toFloat()
                y1 = height.toFloat()
            }
            else -> {
                x0 = 0f
                y0 = 0f
                x1 = width.toFloat()
                y1 = 0f
            }
        }

        LinearGradient(x0, y0, x1, y1, mGradientStartColor, mGradientEndColor, Shader.TileMode.CLAMP)
    }

    private val mRadii: FloatArray by lazy {
        floatArrayOf(
                mLeftTopRadius, mLeftTopRadius,
                mRightTopRadius, mRightTopRadius,
                mRightBottomRadius, mRightBottomRadius,
                mLeftBottomRadius, mLeftBottomRadius
        )
    }

    private val mPath = Path()

    private var ol = 0
    private var or = 0
    private var ot = 0
    private var ob = 0

    init {
        if (attrs != null) {
            val array = getContext().obtainStyledAttributes(attrs, R.styleable.ShadowLayout)
            mShowLeftShadow = array.getBoolean(R.styleable.ShadowLayout_shadow_showLeftShadow, true)
            mShowRightShadow = array.getBoolean(R.styleable.ShadowLayout_shadow_showRightShadow, true)
            mShowBottomShadow = array.getBoolean(R.styleable.ShadowLayout_shadow_showBottomShadow, true)
            mShowTopShadow = array.getBoolean(R.styleable.ShadowLayout_shadow_showTopShadow, true)
            mRadius = array.getDimension(R.styleable.ShadowLayout_shadow_radius, 0f)
            mCircle = array.getBoolean(R.styleable.ShadowLayout_shadow_circle, false)
            mLeftTopRadius = array.getDimension(R.styleable.ShadowLayout_shadow_leftTopRadius, 10f)
            mLeftBottomRadius = array.getDimension(R.styleable.ShadowLayout_shadow_leftBottomRadius, 10f)
            mRightTopRadius = array.getDimension(R.styleable.ShadowLayout_shadow_rightTopRadius, 10f)
            mRightBottomRadius = array.getDimension(R.styleable.ShadowLayout_shadow_rightBottomRadius, 10f)
            mShadowLength = array.getDimension(R.styleable.ShadowLayout_shadow_limit, 10f)
            mShadowOffsetX = array.getDimension(R.styleable.ShadowLayout_shadow_shadowOffsetX, 0f)
            mShadowOffsetY = array.getDimension(R.styleable.ShadowLayout_shadow_shadowOffsetY, 0f)
            mShadowColor = array.getColor(R.styleable.ShadowLayout_shadow_color, Color.parseColor("#2A000000"))
            mBgColor = array.getColor(R.styleable.ShadowLayout_shadow_bgColor, Color.WHITE)
            mShadowMarginLeft = array.getDimension(R.styleable.ShadowLayout_shadow_marginLeft, 0f)
            mShadowMarginRight = array.getDimension(R.styleable.ShadowLayout_shadow_marginRight, 0f)
            mShadowMarginTop = array.getDimension(R.styleable.ShadowLayout_shadow_marginTop, 0f)
            mShadowMarginBottom = array.getDimension(R.styleable.ShadowLayout_shadow_marginBottom, 0f)
            mUseGradient = array.getBoolean(R.styleable.ShadowLayout_shadow_bgUseGradient, mUseGradient)
            mGradientStartColor = array.getColor(R.styleable.ShadowLayout_shadow_bgGradientStartColor, 0)
            mGradientEndColor = array.getColor(R.styleable.ShadowLayout_shadow_bgGradientEndColor, 0)
            mGradientAngle = array.getInt(R.styleable.ShadowLayout_shadow_bgGradientAngle, 0)
            array.recycle()

            if (mRadius > 0) {
                mLeftTopRadius = mRadius
                mLeftBottomRadius = mRadius
                mRightTopRadius = mRadius
                mRightBottomRadius = mRadius
            }
        }
        onInit()
        setWillNotDraw(false)
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
    }

    private fun onInit() {
        var tempOl = mShadowOffsetX
        var tempOt = mShadowOffsetY
        var tempOr = mShadowOffsetX
        var tempOb = mShadowOffsetY

        tempOl += if (mShowLeftShadow) {
            mShadowLength
        } else {
            0f
        }
        tempOr += if (mShowRightShadow) {
            mShadowLength
        } else {
            0f
        }
        tempOt += if (mShowTopShadow) {
            mShadowLength
        } else {
            0f
        }
        tempOb += if (mShowBottomShadow) {
            mShadowLength
        } else {
            0f
        }

        ol = tempOl.toInt()
        or = tempOr.toInt()
        ot = tempOt.toInt()
        ob = tempOb.toInt()

        val pl = paddingLeft + ol
        val pt = paddingTop + ot
        val pr = paddingRight + or
        val pb = paddingBottom + ob

        setPadding(pl, pt, pr, pb)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (childCount < 1) {
            return
        }

        // ======================================================
        // 通过 MeasureSpec.getMode 获取模式存在一个问题:
        // 当获取 MeasureSpec.getMode == MeasureSpec.UNSPECIFIED 时,
        // 即使 xml 宽高设置的是具体值, MeasureSpec.getSize 获取到也还是推荐的值.
        // ======================================================
        // 下面的代码就是为了解决这种情况:
        // 为了避免 MeasureSpec.UNSPECIFIED 模式下获取不到 xml 设置的具体宽高值, 直接从 LayoutParams 中获取
        var width = layoutParams.width
        var height = layoutParams.height

        // 计算剩余空间
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val useWidth = (if (width > 0) width else widthSize)
        val childWidthSpec = MeasureSpec.makeMeasureSpec(useWidth, MeasureSpec.EXACTLY)

        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val child = getChildAt(0)
        measureChild(child, childWidthSpec, heightMeasureSpec)

        // MATCH_PARENT
        if (width == ViewGroup.LayoutParams.MATCH_PARENT) {
            width = widthSize
        }
        if (height == ViewGroup.LayoutParams.MATCH_PARENT) {
            height = heightSize
        }

        // WRAP_CONTENT
        val lp = child.layoutParams as MarginLayoutParams
        if (width == ViewGroup.LayoutParams.WRAP_CONTENT) {
            width = child.measuredWidth + lp.leftMargin + lp.rightMargin + paddingLeft + paddingRight
        }
        if (height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            height = child.measuredHeight + lp.topMargin + lp.bottomMargin + paddingTop + paddingBottom
        }
        setMeasuredDimension(width, height)

        mBgRect.set(ol.toFloat(), ot.toFloat(), width - or.toFloat(), height - ob.toFloat())
    }

    /**
     * childView 圆角限制
     */
    override fun drawChild(canvas: Canvas, child: View?, drawingTime: Long): Boolean {
        val count = canvas.saveLayer(null, null, Canvas.ALL_SAVE_FLAG)

        val b = super.drawChild(canvas, child, drawingTime)

        mPath.reset()
        mPath.addRect(mBgRect, Path.Direction.CCW)
        mPath.addRoundRect(mBgRect, mRadii, Path.Direction.CW)
        mTranPaint.xfermode = xfermode
        canvas.drawPath(mPath, mTranPaint)
        mTranPaint.xfermode = null

        canvas.restoreToCount(count)
        return b
    }

    /**
     * 画 bg
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mUseGradient) {
            mBgPaint.shader = mShaderGradient
        }

        if (mCircle) {
            updateRadii(radius = mBgRect.height() / 2)
        } else {
            val minRadius = Math.min(mBgRect.width(), mBgRect.height())
            updateRadii(minRadius = minRadius / 2)
        }

        mPath.reset()
        mPath.addRoundRect(mBgRect, mRadii, Path.Direction.CCW)
        canvas.drawPath(mPath, mBgPaint)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) {
            setBackgroundCompat(w, h)
        }
    }

    private fun setBackgroundCompat(w: Int, h: Int) {
        val bitmap = createShadowBitmap(w, h)
        background = BitmapDrawable(null, bitmap)
    }

    private fun createShadowBitmap(w: Int, h: Int): Bitmap {
        //优化阴影bitmap大小,将尺寸缩小至原来的1/4。
        val shadowWidth = w / 4
        val shadowHeight = h / 4
        val dx = mShadowOffsetX / 4
        val dy = mShadowOffsetY / 4

        var _ol = ol / 4f
        var _or = or / 4f
        var _ot = ot / 4f
        var _ob = ob / 4f

        _ol += if (!mShowLeftShadow) {
            mShadowLength / 4
        } else {
            0f
        }
        _or += if (!mShowRightShadow) {
            mShadowLength / 4
        } else {
            0f
        }
        _ot += if (!mShowTopShadow) {
            mShadowLength / 4
        } else {
            0f
        }
        _ob += if (!mShowBottomShadow) {
            mShadowLength / 4
        } else {
            0f
        }

        val output = Bitmap.createBitmap(shadowWidth, shadowHeight, Bitmap.Config.ARGB_4444)
        val canvas = Canvas(output)
        mShadowRect.set(_ol, _ot, shadowWidth - _or, shadowHeight - _ob)
        mShadowRect.top += mShadowMarginTop / 4 + 0.5f
        mShadowRect.bottom -= mShadowMarginBottom / 4 + 0.5f
        mShadowRect.left += mShadowMarginLeft / 4 + 0.5f
        mShadowRect.right -= mShadowMarginRight / 4 + 0.5f

        // * 0.8 避免边缘出现明显分界线
        mShadowPaint.setShadowLayer(mShadowLength / 4 * 0.8f, dx, dy, mShadowColor)

        if (mCircle) {
            updateRadii(radius = mShadowRect.height() / 2)
        } else {
            val minRadius = Math.min(mShadowRect.width(), mShadowRect.height())
            updateRadii(minRadius = minRadius / 2, scale = 0.25f)
        }

        mPath.reset()
        mPath.addRoundRect(mShadowRect, mRadii, Path.Direction.CCW)
        canvas.drawPath(mPath, mShadowPaint)

        return output
    }

    private fun updateRadii(radius: Float = 0f, minRadius: Float = 0f, scale: Float = 1f) {
        var leftTopRadius = if (radius > 0) {
            radius
        } else {
            mLeftTopRadius
        }
        var leftBottomRadius = if (radius > 0) {
            radius
        } else {
            mLeftBottomRadius
        }
        var rightTopRadius = if (radius > 0) {
            radius
        } else {
            mRightTopRadius
        }
        var rightBottomRadius = if (radius > 0) {
            radius
        } else {
            mRightBottomRadius
        }

        leftTopRadius *= scale
        leftBottomRadius *= scale
        rightTopRadius *= scale
        rightBottomRadius *= scale

        if (minRadius > 0) {
            leftTopRadius = Math.min(leftTopRadius, minRadius)
            leftBottomRadius = Math.min(leftBottomRadius, minRadius)
            rightTopRadius = Math.min(rightTopRadius, minRadius)
            rightBottomRadius = Math.min(rightBottomRadius, minRadius)
        }

        mRadii[0] = leftTopRadius
        mRadii[1] = leftTopRadius
        mRadii[2] = rightTopRadius
        mRadii[3] = rightTopRadius
        mRadii[4] = rightBottomRadius
        mRadii[5] = rightBottomRadius
        mRadii[6] = leftBottomRadius
        mRadii[7] = leftBottomRadius
    }

}