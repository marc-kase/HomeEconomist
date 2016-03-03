package kz.gereski.m.homebank.domain;

public class NavItem {
    public String mTitle;
    public String mSubtitle;
    public int mIcon;
    private NavProcessor mProcessor;

    public NavItem(String title, String subtitle, int icon/*, NavProcessor mProcessor*/) {
        mTitle = title;
        mSubtitle = subtitle;
        mIcon = icon;
//        this.mProcessor = mProcessor;
    }

    public void start() {
        mProcessor.start();
    }
}
