package com.beyole.view;

import com.beyole.qqlistview.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;

public class QQListView extends ListView {

	private static final String TAG = "QQListView";

	// 用户滑动的最小距离
	private int touchSlop;

	// 是否响应滑动
	private boolean isSliding;
	// 手指按下时的x的坐标
	private int xDown;
	// 手指按下时的y的坐标
	private int yDown;
	// 手指移动时的x的坐标
	private int xMove;
	// 手指移动时的y的坐标
	private int yMove;

	private LayoutInflater mInflater;

	private PopupWindow mPopupWindow;
	private int mPopupWindowHeight;
	private int mPopupWindowWidth;

	private Button mButton;

	private OnDelBtnClickListener mListener;
	// 当前手指触碰的view
	private View mCurrentView;
	// 当前手指触碰的位置
	private int mCurrentViewPos;

	public QQListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mInflater = LayoutInflater.from(context);
		// 触发移动事件的最短距离，如果小于这个距离就不触发移动控件
		touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
		// 获取deleteBtn的view
		View view = mInflater.inflate(R.layout.delete_btn, null);
		mButton = (Button) view.findViewById(R.id.id_item_btn);
		// 将我们定义的删除view编程popupwindow
		mPopupWindow = new PopupWindow(view, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		// 先调一下measure，否则拿不到宽和高
		mPopupWindow.getContentView().measure(0, 0);
		// 获取popupwindow的宽度和高度
		mPopupWindowHeight = mPopupWindow.getContentView().getMeasuredHeight();
		mPopupWindowWidth = mPopupWindow.getContentView().getMeasuredWidth();
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		int action = ev.getAction();
		int x = (int) ev.getX();
		int y = (int) ev.getY();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			xDown = x;
			yDown = y;
			/**
			 * 如果当前popupwindow显示，则直接隐藏掉，然后屏蔽listview的touch事件的下传
			 */
			if (mPopupWindow.isShowing()) {
				dismissPopWindow();
				return false;
			}
			// 获得当前手指按下时的item的位置
			mCurrentViewPos = pointToPosition(xDown, yDown);
			// 获得当前手指按下的item
			View view = getChildAt(mCurrentViewPos - getFirstVisiblePosition());
			mCurrentView = view;
			break;
		case MotionEvent.ACTION_MOVE:
			xMove = x;
			yMove = y;
			// 获取偏移量
			int dx = xMove - xDown;
			int dy = yMove - yDown;
			// 判断是否从右滑到左
			if (xMove < xDown && Math.abs(dx) > touchSlop && Math.abs(dy) < touchSlop) {
				isSliding = true;
			}
			break;

		}
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		int action = ev.getAction();
		/**
		 * 如果是从左到右的滑动才会响应
		 */
		if (isSliding) {
			switch (action) {
			case MotionEvent.ACTION_MOVE:
				int[] location = new int[2];
				// 获得当前item的x和y位置
				mCurrentView.getLocationOnScreen(location);
				// 设置popupwinodw的动画
				 mPopupWindow.setAnimationStyle(R.style.popwindow_delete_btn_anim_style);
				mPopupWindow.update();
				mPopupWindow.showAtLocation(mCurrentView, Gravity.LEFT | Gravity.TOP, location[0] + mCurrentView.getWidth(), location[1] + mCurrentView.getHeight() / 2 - mPopupWindowHeight / 2);
				// 设置删除按钮的回调
				mButton.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (mListener != null) {
							mListener.clickHappend(mCurrentViewPos);
							mPopupWindow.dismiss();
						}
					}
				});
				break;
			case MotionEvent.ACTION_UP:
				isSliding = false;
				break;
			}
			// 相应滑动期间屏幕itemclick事件，避免发生冲突 向下分发
			return true;
		}

		return super.onTouchEvent(ev);
	}

	/**
	 * 隐藏popupwindow
	 */
	private void dismissPopWindow() {
		if (mPopupWindow != null && mPopupWindow.isShowing()) {
			mPopupWindow.dismiss();
		}
	}

	public void setOnDelBtnClickListener(OnDelBtnClickListener listener) {
		mListener = listener;
	}

	public interface OnDelBtnClickListener {
		public void clickHappend(int position);
	}
}
