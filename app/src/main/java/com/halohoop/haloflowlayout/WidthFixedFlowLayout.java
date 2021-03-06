package com.halohoop.haloflowlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import static android.R.attr.columnCount;
import static android.content.ContentValues.TAG;
import static com.halohoop.haloflowlayout.R.attr.column;

/**
 * Created by Pooholah on 2017/9/2.
 */

public class WidthFixedFlowLayout extends ViewGroup implements View.OnClickListener {
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
     * 每个子view的宽度，固定了{@link WidthFixedFlowLayout#mEachotherMarginX}之后就能够固定下来了
     */
    private int mChildWidth;

    public WidthFixedFlowLayout(Context context) {
        this(context, null);
    }

    public WidthFixedFlowLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WidthFixedFlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.WidthFixedFlowLayout);
        mColumn = ta.getInt(R.styleable.WidthFixedFlowLayout_column, 4);
        mEachotherMarginX = ta.getDimensionPixelOffset(R.styleable
                .WidthFixedFlowLayout_margin_eachother_x, 5);
        mEachotherMarginY = ta.getDimensionPixelOffset(R.styleable
                .WidthFixedFlowLayout_margin_eachother_y, 5);
        ta.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
//        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        final int paddingStart =
                android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1 ?
                        getPaddingStart() : getPaddingLeft();
        final int paddingEnd =
                android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1 ?
                        getPaddingEnd() : getPaddingRight();
        //列数
        final int column = getColumns();
        final int eachotherMarginX = mEachotherMarginX;
        mChildWidth = getChildWidth(widthSize, paddingStart, paddingEnd, column, eachotherMarginX);

        //针对wrap_content的时候
//        int width = 0;//宽度不考虑wrap_content，因为是固定列数的
        int height = 0;
        final int childCount = getChildCount();
        //行数
        final int lines = getLines(childCount, column);
        final int extra = childCount % column;
        for (int i = 0; i < lines; i++) {
            int columnCount = getCurrLineCount(column, lines, extra, i);
            int maxHeightInLine = 0;
            for (int j = 0; j < columnCount; j++) {
                int index = i * column + j;
                View child = getChildAt(index);
                measureChild(child, widthMeasureSpec, heightMeasureSpec);
                MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
                int topMargin = lp.topMargin;
                int bottomMargin = lp.bottomMargin;
                int measuredHeight = child.getMeasuredHeight();
                maxHeightInLine = Math.max(maxHeightInLine,
                        measuredHeight + topMargin + bottomMargin);
            }
            //一行结束之后
            height += maxHeightInLine;
            if (i != lines - 1) {//如果不是最后一行的最后
                height += mEachotherMarginY;
            }
        }

        setMeasuredDimension(
                widthSize,
                heightMode == MeasureSpec.EXACTLY ?
                        heightSize : height + getPaddingTop() + getPaddingBottom()
        );
    }

    private int getCurrLineCount(int column, int lines, int extra, int i) {
        return i != lines - 1 ? column : (extra == 0 ? column : extra);
    }

    /**
     * 得到每个子view应该有的宽度
     * 独立出来方便单元测试
     * @param widthSize
     * @param paddingStart
     * @param paddingEnd
     * @param column
     * @param eachotherMarginX
     * @return
     */
    private int getChildWidth(int widthSize, int paddingStart, int paddingEnd,
                              int column, int eachotherMarginX) {
        int widthLeft = widthSize - paddingStart - paddingEnd;
        int marginSum = eachotherMarginX * (column - 1);
        widthLeft -= marginSum;
        return widthLeft / column;
    }

    /**
     * 独立出来方便单元测试
     *
     * @param childCount
     * @param column
     * @return
     */
    public int getLines(int childCount, int column) {
        return childCount / column + (childCount % column == 0 ? 0 : 1);
    }

    public int getColumns() {
        return mColumn;
    }

    public void setColumn(int column) {
        if (column == mColumn) {
            return;
        }
        this.mColumn = column;
        requestLayout();
    }

    public int getChildWidth() {
        return mChildWidth;
    }

    public int getEachotherMarginX() {
        return mEachotherMarginX;
    }

    public void setEachotherMarginX(int eachotherMarginX) {
        this.mEachotherMarginX = eachotherMarginX;
        requestLayout();
    }

    public int getEachotherMarginY() {
        return mEachotherMarginY;
    }

    public void setEachotherMarginY(int eachotherMarginY) {
        this.mEachotherMarginY = eachotherMarginY;
        requestLayout();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int childCount = getChildCount();
        int pointerX =
                android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1 ?
                        getPaddingStart() : getPaddingLeft();
        int pointerY = getPaddingTop();

        final int column = getColumns();
        final int lines = getLines(childCount, column);
        final int extra = childCount % column;
        for (int i = 0; i < lines; i++) {
//            int columnCount = i != lines - 1 ? column : extra;//有bug
            int columnCount = getCurrLineCount(column, lines, extra, i);
            int maxLineHeightHold = 0;
            for (int j = 0; j < columnCount; j++) {
                int index = i * column + j;
                View child = getChildAt(index);
                MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
                int left;
                int top;
                int right;
                int bottom;
                left = pointerX;
                top = pointerY + lp.topMargin;
                right = left + mChildWidth;
                bottom = top + child.getMeasuredHeight();
                Log.i(TAG, TAG + "onLayout: " + left + "-"+top + "-" + right + "-" + bottom);
                child.layout(left, top, right, bottom);

                if (i == (lines - 1) && j == (columnCount - 1)) {//最后一个的时候不需要在矫正位置了
                    Log.i(TAG, TAG + "最后一个break");
                    break;
                }

                maxLineHeightHold = Math.max(bottom + lp.bottomMargin, maxLineHeightHold);
                //定位下一次的开始点
                if (j != (columnCount - 1)) {
                    pointerX = right + mEachotherMarginX;
                } else {
                    Log.i(TAG, TAG + "换行");
                    pointerX =
                            android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES
                                    .JELLY_BEAN_MR1 ?
                                    getPaddingStart() : getPaddingLeft();
                    pointerY = maxLineHeightHold + mEachotherMarginY;
                }
            }
        }
    }

    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        final int childCount = getChildCount();
        if (onItemClickListener!=null) {
            this.mOnItemClickListener = onItemClickListener;
            for (int i = 0; i < childCount; i++) {
                View child = getChildAt(i);
                child.setOnClickListener(this);
            }
        }else{
            for (int i = 0; i < childCount; i++) {
                View child = getChildAt(i);
                child.setOnClickListener(null);
            }
        }
    }

    @Override
    public void onClick(View v) {
        int index = indexOfChild(v);
        View child = getChildAt(index);
        if (this.mOnItemClickListener != null && index != -1) {
            this.mOnItemClickListener.onItemClick(this, index, child);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(WidthFixedFlowLayout flowLayout, int index, View v);
    }

    private static final String TAG = "WidthFixedFlowLayout--";

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
