package com.satra.traveler;

import com.stephentuso.welcome.BasicPage;
import com.stephentuso.welcome.WelcomeActivity;
import com.stephentuso.welcome.WelcomeConfiguration;

public class IntroActivity extends WelcomeActivity {

    @Override
    protected WelcomeConfiguration configuration() {
        return new WelcomeConfiguration.Builder(this)
                .defaultBackgroundColor(R.color.colorPrimary)
                //.defaultHeaderTypefacePath("fonts/digital-7.ttf")
                .page(new BasicPage(R.drawable.ic_detect,
                        getString(R.string.title_detect),
                        getString(R.string.intro_detect))
                )
                .page(new BasicPage(R.drawable.ic_predict,
                        getString(R.string.title_predict),
                        getString(R.string.intro_predict))
                        .background(R.color.bluePrimary)
                )
                .page(new BasicPage(R.drawable.ic_notify,
                        getString(R.string.title_notify),
                        getString(R.string.intro_notify))
                ).page(new BasicPage(R.drawable.ic_loyalty,
                        getString(R.string.rewards_title),
                        getString(R.string.rewards_content))
                        .background(R.color.bluePrimary)
                )
                .animateButtons(true)
                .swipeToDismiss(true)
                .build();
    }

}
