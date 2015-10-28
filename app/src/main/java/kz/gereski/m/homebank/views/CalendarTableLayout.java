package kz.gereski.m.homebank.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TableLayout;

public class CalendarTableLayout extends TableLayout {
    private int action = -1;
    private OnCalendarSwiped onCalendarSwiped;
    private float x0, y0;
    private final float SWIPE_THRESHOLD = 10;
    public enum Direction {NONE, UP, DOWN, LEFT, RIGHT}

    public CalendarTableLayout(Context context) {
        super(context);
    }

    public CalendarTableLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setOnCalendarSwipedListener(OnCalendarSwiped onCalendarSwiped) {
        this.onCalendarSwiped = onCalendarSwiped;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int act = ev.getAction();
        if (act == MotionEvent.ACTION_DOWN) {
            x0 = ev.getX();
            y0 = ev.getY();
        }
        if (act == MotionEvent.ACTION_MOVE) {
            action = MotionEvent.ACTION_MOVE;
            return false;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean actionEnded = ev.getAction() == MotionEvent.ACTION_UP
                || ev.getAction() == MotionEvent.ACTION_CANCEL;
        if (actionEnded && action == MotionEvent.ACTION_MOVE) {
            action = -1;
            float dX = ev.getX() - x0;
            float dY = ev.getY() - y0;

            if (!(Math.abs(dX) > SWIPE_THRESHOLD
                    || Math.abs(dY) > SWIPE_THRESHOLD))
                return super.dispatchTouchEvent(ev);

            Direction direction = Direction.NONE;
            if (Math.abs(dX) > Math.abs(dY)) {
                if (dX < 0) direction = Direction.RIGHT;
                else direction = Direction.LEFT;
            } else if (Math.abs(dX) < Math.abs(dY)) {
                if (dY > 0) direction = Direction.DOWN;
                else direction = Direction.UP;
            }
            System.out.println("SWIPED!!!");
            onCalendarSwiped.doSwipe(direction);
            return false;
        }
        else return super.dispatchTouchEvent(ev);
    }

    public interface OnCalendarSwiped {
        void doSwipe(Direction direction);
    }
}


