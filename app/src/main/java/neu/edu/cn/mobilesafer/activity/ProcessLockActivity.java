package neu.edu.cn.mobilesafer.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import neu.edu.cn.mobilesafer.R;
import neu.edu.cn.mobilesafer.db.dao.AppInfo;
import neu.edu.cn.mobilesafer.db.dao.AppLockDao;
import neu.edu.cn.mobilesafer.engine.AppInfoProvider;

public class ProcessLockActivity extends AppCompatActivity {
    // 未加锁按钮
    private Button mUnlockedButton;
    // 已加锁按钮
    private Button mLockedButton;
    // 未加锁程序列表所在的线性布局
    private LinearLayout mLinearUnLockedLayout;
    // 未加锁列表线性布局中描述信息文本框
    private TextView mLinearUnlockedText;
    // 未加锁列表线性布局中的ListView
    private ListView mLinearUnlockedListView;
    // 已加锁程序列表所在的线性布局
    private LinearLayout mLinearLockedLayout;
    // 已加锁列表线性布局中描述信息文本框
    private TextView mLinearLockedText;
    // 已加锁列表线性布局中的ListView
    private ListView mLinearLockedListView;
    // 数据库中获取到已加锁的应用的包名的集合
    private List<String> mPackageNameList;
    // 操作数据库的对象
    private AppLockDao mAppLockDao;
    // 未加锁的应用集合
    private List<AppInfo> mUnLockedAppList;
    // 已加锁的应用集合
    private List<AppInfo> mLockedAppList;
    // 已加锁的AppLock的数据适配器
    private AppLockAdapter mAppLockedAdapter;
    // 未加锁的AppLock的数据适配器
    private AppLockAdapter mAppUnLockAdapter;
    // 当前手机安装的所有应用集合
    private List<AppInfo> mAppInfoList;
    // 平移动画
    private TranslateAnimation mAnimation;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (mAppLockedAdapter == null) {
                mAppLockedAdapter = new AppLockAdapter(true);
                mLinearLockedListView.setAdapter(mAppLockedAdapter);
            }
            if (mAppUnLockAdapter == null) {
                mAppUnLockAdapter = new AppLockAdapter(false);
                mLinearUnlockedListView.setAdapter(mAppUnLockAdapter);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_lock);
        // 初始化布局文件中的view
        initView();
        // 初始化数据
        initData();
        // 初始化平移动画
        initAnimation();
    }

    /**
     * 初始化列表数据
     */
    private void initData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mAppLockDao = AppLockDao.getInstance(getApplicationContext());
                mLockedAppList = new ArrayList<AppInfo>();
                mUnLockedAppList = new ArrayList<AppInfo>();
                // 获取到已加锁的应用的包名集合
                mPackageNameList = mAppLockDao.findAll();
                // 获取手机已安装的所有应用集合
                mAppInfoList = AppInfoProvider.getAppInfoList(getApplicationContext());
                for (AppInfo appInfo : mAppInfoList) {
                    // 如果当前应用的包名在已加锁的包名集合中，将当前应用加入到已加锁的应用集合
                    if (mPackageNameList.contains(appInfo.packageName)) {
                        mLockedAppList.add(appInfo);
                    } else {
                        // 如果当前应用的包名不在已加锁的包名集合中，将当前应用加入到未加锁的应用集合
                        mUnLockedAppList.add(appInfo);
                    }
                }
                mHandler.sendEmptyMessage(0);
            }
        }).start();
    }

    /**
     * 初始化布局文件中的view
     */
    private void initView() {
        mUnlockedButton = (Button) findViewById(R.id.unlocked_progress_button);
        mLockedButton = (Button) findViewById(R.id.locked_progress_button);
        mLinearUnLockedLayout = (LinearLayout) findViewById(R.id.linear_unlocked_layout);
        mLinearLockedLayout = (LinearLayout) findViewById(R.id.linear_locked_layout);
        mLinearUnlockedListView = (ListView) findViewById(R.id.unlocked_progress_list);
        mLinearLockedListView = (ListView) findViewById(R.id.locked_progress_list);
        mLinearUnlockedText = (TextView) findViewById(R.id.unlocked_title_text);
        mLinearLockedText = (TextView) findViewById(R.id.locked_title_text);
        // 点击未加锁按钮切换到已加锁列表
        mUnlockedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLinearLockedLayout.setVisibility(View.GONE);
                mLinearUnLockedLayout.setVisibility(View.VISIBLE);
                mUnlockedButton.setBackgroundResource(R.drawable.tab_left_pressed);
                mLockedButton.setBackgroundResource(R.drawable.tab_right_default);
                // 切换已加锁和未加锁表单时,刷新数据适配器
                mAppUnLockAdapter.notifyDataSetChanged();
            }
        });
        // 点击已加锁按钮切换到未加锁列表
        mLockedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLinearUnLockedLayout.setVisibility(View.GONE);
                mLinearLockedLayout.setVisibility(View.VISIBLE);
                mLockedButton.setBackgroundResource(R.drawable.tab_right_pressed);
                mUnlockedButton.setBackgroundResource(R.drawable.tab_left_default);
                // 切换已加锁和未加锁表单时,刷新数据适配器
                mAppLockedAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * 初始化平移动画
     */
    private void initAnimation() {
        // 平移动画，X轴相对于自身平移自身最大宽度，Y轴相对于自身保持不变
        mAnimation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 1,
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0);
        mAnimation.setDuration(300);
    }

    class AppLockAdapter extends BaseAdapter {
        // 适配器根据传入的参数，用于区分已加锁和未加锁列表适配器
        private boolean isLock;

        public AppLockAdapter(boolean isLock) {
            this.isLock = isLock;
        }

        @Override
        public int getCount() {
            if (isLock) {
                mLinearLockedText.setText("已加锁应用:" + mLockedAppList.size());
                return mLockedAppList.size();
            } else {
                mLinearUnlockedText.setText("未加锁应用:" + mUnLockedAppList.size());
                return mUnLockedAppList.size();
            }
        }

        @Override
        public AppInfo getItem(int position) {
            if (isLock) {
                return mLockedAppList.get(position);
            } else {
                return mUnLockedAppList.get(position);
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = View.inflate(getApplicationContext(), R.layout.app_lock_item, null);
                viewHolder.appLockIcon = (ImageView) convertView.findViewById(R.id.app_lock_icon);
                viewHolder.appLockNameText = (TextView) convertView.findViewById(R.id.app_lock_name);
                viewHolder.appLockImage = (ImageView) convertView.findViewById(R.id.app_lock_image);
                convertView.setTag(viewHolder);

            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            final View view = convertView;
            final AppInfo appinfo = getItem(position);
            viewHolder.appLockIcon.setBackgroundDrawable(appinfo.icon);
            viewHolder.appLockNameText.setText(appinfo.appName);
            // 由已加锁或未加锁数据适配器，设置程序所对应图片
            if (isLock) {
                viewHolder.appLockImage.setImageResource(R.drawable.lock);
            } else {
                viewHolder.appLockImage.setImageResource(R.drawable.unlock);
            }
            viewHolder.appLockImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 点击之后开启平移动画,并设置动画监听事件
                    view.startAnimation(mAnimation);
                    mAnimation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            if (isLock) {
                                // 已加锁,已加锁列表移除一个，未加锁列表增加一个
                                mLockedAppList.remove(appinfo);
                                mUnLockedAppList.add(0, appinfo);
                                mAppLockDao.delete(appinfo.packageName);
                                mAppLockedAdapter.notifyDataSetChanged();
                            } else {
                                // 未加锁,未加锁列表移除一个，已加锁列表增加一个
                                mLockedAppList.add(0, appinfo);
                                mUnLockedAppList.remove(appinfo);
                                mAppLockDao.insert(appinfo.packageName);
                                mAppUnLockAdapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }
                    });
                }
            });
            return convertView;
        }
    }

    static class ViewHolder {
        ImageView appLockIcon;
        TextView appLockNameText;
        ImageView appLockImage;
    }
}
