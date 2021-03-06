package qiu.niorgai.refreshview.bottom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ScrollView;

/**
 * 自动加载更多的ScrollView
 * Created by qiu on 9/20/15.
 */
public class AutoLoadScrollView extends ScrollView implements LoadMoreInterface.AutoLoadView{

    //是否正在加载
    private boolean isLoadingMore = false;
    //是否有更多
    private boolean isHaveMore = false;

    private LoadMoreInterface.onLoadMoreListener loadMoreListener;

    private BottomLoadingView mLoadingView;

    public AutoLoadScrollView(Context context) {
        this(context, null);
    }

    public AutoLoadScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mLoadingView = new BottomLoadingView(context);
    }

    public interface onScrolledListener {
        void scrollChanged(int l, int t, int oldl, int oldt);
    }

    private onScrolledListener listener;

    public void setListener(onScrolledListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        //布局成功后初始化LoadingView
        final ViewGroup group = getChildViewGroup();
        if (group == null) {
            //未设置子View则不监听滑动
            isHaveMore = false;
            return;
        }
        if (isHaveMore) {
            int scrollViewHeight = getMeasuredHeight();
            int childViewHeight = group.getMeasuredHeight();
            if (scrollViewHeight == childViewHeight) {
                //此时未填满屏幕
                group.addView(mLoadingView, group.getChildCount());
                mLoadingView.changeToClickStatus(loadMoreListener);
            } else {
                //填满屏幕了
                group.addView(mLoadingView, group.getChildCount());
                mLoadingView.changeToLoadingStatus();
            }
        }
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (listener != null) {
            listener.scrollChanged(l, t, oldl, oldt);
        }
        //需要监听滑动
        if (isHaveMore && !isLoadingMore && loadMoreListener != null) {
            if (t > oldt) {
                //正在向下滑动
                if (t + getHeight() >= computeVerticalScrollRange() - mLoadingView.getMeasuredHeight() / 2) {
                    //已经滑动到底部
                    loadMoreListener.onLoadMore();
                    isLoadingMore = true;
                    mLoadingView.changeToLoadingStatus();
                }
            }
        }
    }

    @Override
    public void onSuccess(boolean hasMore) {
        isLoadingMore = false;
        isHaveMore = hasMore;
        if (!isHaveMore) {
            mLoadingView.changeToHideStatus();
        } else {
            final ViewGroup group = getChildViewGroup();
            if (group == null) {
                //未设置子View则不监听滑动
                isHaveMore = false;
                return;
            }
            int scrollViewHeight = getMeasuredHeight();
            int childViewHeight = group.getMeasuredHeight();
            if (scrollViewHeight == childViewHeight) {
                //此时未填满屏幕
                mLoadingView.changeToClickStatus(loadMoreListener);
            } else {
                //填满屏幕了
                mLoadingView.changeToLoadingStatus();
            }
        }
    }

    @Override
    public void onFailure() {
        isLoadingMore = false;
        if (isHaveMore) {
            mLoadingView.changeToClickStatus(loadMoreListener);
        }
    }

    @Override
    public void setIsHaveMore(boolean isHaveMore) {
        this.isHaveMore = isHaveMore;
    }

    @Override
    public void setOnLoadMoreListener(LoadMoreInterface.onLoadMoreListener listener) {
        this.loadMoreListener = listener;
    }

    //获取子ViewGroup
    private ViewGroup getChildViewGroup() {
        return getChildCount() == 0 ? null : (ViewGroup)getChildAt(0);
    }

}
