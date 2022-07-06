package com.dark.component;

import java.awt.Color;
import java.awt.Font;
import javax.swing.JTextField;

public class TextComponentMenuDemoApp {
    public static void main(String[] args) {
        TextComponentManager menu = new TextComponentManager.MenuBuilder(new JTextField())
                .setFont(new Font("Ubuntu Mono", Font.BOLD, 15))
                .setTextColor(Color.BLACK)
                .setMenuType(TextComponentMenuType.MAXIMUM)
                .setPadding(new int[]{1, 1, 1, 100})
                .setEnableFindError(false)
                .build();
        
        menu.show();
    }
}
