package com.halohoop.haloflowlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Halohoop on 2017/8/31.
 * 流式布局，可以设置列数，目前用于场景页面
 * 每个子view都是一样宽的!
 */

public class FlowLayout extends ViewGroup {

    /**
     * 有多少列
     */
    private int mColumn;
    /**
     * 彼此间隔，不包含子view自身设置的margin值
     */
    private int mEachotherMarginX;
    /**
     * 上下间隔，不包含子view自身设置的margin值
     */
    private int mEachotherMarginY;

    /**
     * 每个子view的宽度，固定了{@link FlowLayout#mEachotherMarginX}之后就能够固定下来了
     */
    private int mChildWidth;

    public FlowLayout(Context context) {
        this(context, null);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.WidthFixedFlowLayout);
        mColumn = ta.getInt(R.styleable.WidthFixedFlowLayout_column, 4);
        mEachotherMarginX = ta.getDimensionPixelOffset(R.styleable
                .WidthFixedFlowLayout_margin_eachother_x, 5);
        mEachotherMarginY = ta.getDimensionPixelOffset(R.styleable
                .WidthFixedFlowLayout_margin_eachother_y, 5);
        ta.recycle();
    }

    public int getColumn() {
        return mColumn;
    }

    public void setColumn(int column) {
        this.mColumn = column;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
        int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);

        int eachotherMarginSum = mEachotherMarginX * (mColumn - 1);//如果有4列那就有3个空隙
        //剩余可用宽度
        int sizeWidthLeft = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            sizeWidthLeft = sizeWidth - getPaddingStart() - getPaddingEnd() - eachotherMarginSum;
        } else {
            sizeWidthLeft = sizeWidth - getPaddingLeft() - getPaddingRight() - eachotherMarginSum;
        }
        mChildWidth = sizeWidthLeft / mColumn;

        // wrap_content的时候，需要计算出宽高
//        int width = 0;
        int height = 0;

        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            //测量子view高度
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
            MarginLayoutParams lp = (MarginLayoutParams) child
                    .getLayoutParams();
            int childHeightHold = child.getMeasuredHeight() + lp.topMargin
                    + lp.bottomMargin;
            int isLineLast = (i + 1) % mColumn;
            if (isLineLast == 0) {//露了如果只有一行且不足mColumn个的时候，所以下面的if来补充
                height += childHeightHold;
                if (i != childCount - 1) {//
                    height += mEachotherMarginY;
                }
            }
        }
        int extra = childCount % mColumn;
        if (extra != 0 && extra < mColumn) {//if来补充
            View child = getChildAt(0);
            MarginLayoutParams lp = (MarginLayoutParams) child
                    .getLayoutParams();
            int childHeightHold = child.getMeasuredHeight() + lp.topMargin
                    + lp.bottomMargin;
            height += childHeightHold;
        }
//        height += getPaddingTop();//最后setMeasuredDimension的时候加入即可

        setMeasuredDimension(
                //宽度暂时不考虑wrapcontent
//                modeWidth == MeasureSpec.EXACTLY ?
//                        sizeWidth : width + getPaddingLeft() + getPaddingRight(),
                sizeWidth,
                modeHeight == MeasureSpec.EXACTLY ?
                        sizeHeight : height + getPaddingTop() + getPaddingBottom()
        );
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
            final int childCount = getChildCount();
            int layoutPointerX =
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 ?
                            getPaddingStart() : getPaddingLeft();
        int layoutPointerY = getPaddingTop();

        int extra = childCount % mColumn;
        //得到有多少行
        int lines = childCount / mColumn
                + (extra == 0 ? 0 : 1);//除不尽证明不是矩阵，需要加多一行
        for (int i = 0; i < lines; i++) {//行
            for (int j = 0; j < mColumn; j++) {//列
                int index = i * mColumn + j;//集合中的序号index
                View child = getChildAt(index);
                MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
                child.layout(layoutPointerX,
                        i != 0 ? layoutPointerY + lp.topMargin + mEachotherMarginY//不是第一行
                                : layoutPointerY + lp.topMargin,//是第一行
                        layoutPointerX + mChildWidth,
                        i != 0 ? layoutPointerY + lp.topMargin + mEachotherMarginY + child.getMeasuredHeight()
                                : layoutPointerY + lp.topMargin + child.getMeasuredHeight());
                if ((childCount - 1) == index) {//如果是最后一个
                    break;
                }
                layoutPointerX += mEachotherMarginX + mChildWidth;
                if ((index + 1) % mColumn == 0) {
                    layoutPointerY += child.getMeasuredHeight()
                            + lp.bottomMargin + lp.topMargin;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        layoutPointerX = getPaddingStart();
                    }else{
                        layoutPointerX = getPaddingLeft();
                    }
                }
            }
        }
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
    }
}
