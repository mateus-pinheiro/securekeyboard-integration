package com.globile.santander.mobisec.securekeyboard;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;

import com.globile.santander.mobisec.securekeyboard.enums.InputLanguage;
import com.globile.santander.mobisec.securekeyboard.keyboard.SanKeyboardManager;
import com.globile.santander.mobisec.securekeyboard.utils.SanKeyboardUtils;

public class SelectLanguageDialog {

    private SelectLanguageDialog() {
    }

    static void showSelectLanguageDialog(Activity activity, InputLanguage currentLanguage,
            DialogInterface.OnClickListener onPositiveClickListener, DialogInterface.OnDismissListener onDismissListener) {

        if (SanKeyboardManager.getPossibleLanguages().length < 2 && activity == null) {
            return; //No languages to change to
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setTitle(activity.getString(R.string.securekeyboard_title_select_keyboard_language))
                .setSingleChoiceItems(getSingleChoiceItems(activity, currentLanguage),
                        currentLanguage.ordinal(), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int selectedIndex) {
                                // Nothing to do
                            }
                        })
                .setPositiveButton(activity.getString(R.string.securekeyboard_OK),
                        onPositiveClickListener)
                .setNegativeButton(activity.getString(R.string.securekeyboard_cancel), null);

        AlertDialog alertDialog = builder.create();
        alertDialog.setOnDismissListener(onDismissListener);

        alertDialog.show();

    }

    private static String[] getSingleChoiceItems(Activity activity, InputLanguage currentLanguage) {

        String[] singleChoiceItems = new String[InputLanguage.values().length];

        Context contextLocalized = SanKeyboardUtils.getContextForLocale(activity, currentLanguage.getLocale());

        //0 = English, 1 = Spanish, 2 = Portuguese, 3 = Polish
        for (int i = 0; i < InputLanguage.values().length; i++) {
            InputLanguage language = InputLanguage.values()[i];
            singleChoiceItems[i] = language.getText(contextLocalized.getResources());
        }

        return singleChoiceItems;

    }

}
