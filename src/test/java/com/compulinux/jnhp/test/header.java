package com.compulinux.jnhp.test;

import com.compulinux.jnhp.Jnhp;
import com.compulinux.jnhp.runApplication;

public class header implements runApplication {
    @Override
    public void runApp(Jnhp html) throws Exception {
        html.setVar("title", " Top Level title");
    }
}
