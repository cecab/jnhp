package com.compulinux.jnhp.test;

import com.compulinux.jnhp.Jnhp;
import com.compulinux.jnhp.runApplication;

public class footer implements runApplication {
    @Override
    public void runApp(Jnhp html) throws Exception {
        html.setVar("contact", "You can reach out at cc@com.us");
    }
}
