package com.globile.santander.mobisec.securekeyboard;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.globile.santander.mobisec.securekeyboard.aosp.KeyboardAOSP;
import com.globile.santander.mobisec.securekeyboard.aosp.KeyboardViewAOSP;
import com.globile.santander.mobisec.securekeyboard.enums.InputLanguage;
import com.globile.santander.mobisec.securekeyboard.enums.ShiftMode;
import com.globile.santander.mobisec.securekeyboard.enums.SpaceKeyState;
import com.globile.santander.mobisec.securekeyboard.enums.TopRowButtonsOptions;
import com.globile.santander.mobisec.securekeyboard.keyboard.SanKeyboard;
import com.globile.santander.mobisec.securekeyboard.keyboard.SanKeyboardManager;
import com.globile.santander.mobisec.securekeyboard.keyboard.SanKeyboardType;
import com.globile.santander.mobisec.securekeyboard.listeners.SanEventCallbacks;
import com.globile.santander.mobisec.securekeyboard.listeners.SanTapJackedCallback;
import com.globile.santander.mobisec.securekeyboard.utils.AnimationsManager;
import com.globile.santander.mobisec.securekeyboard.utils.SanKeyboardUtils;
import com.globile.santander.mobisec.securekeyboard.utils.SpaceKeyStateManager;

import java.util.Timer;
import java.util.TimerTask;

public class SanKeyboardView extends KeyboardViewAOSP implements KeyboardViewAOSP.OnKeyboardActionListener {

    // INTERFACES
    // ***********************************************************************

    public interface SanKeyboardCallback {

        void onContinueClick();

        void onCancelClick();

    }

    // ATTRIBUTES
    // ***********************************************************************

    private static final long CHANGE_LANGUAGE_WAIT = 1500L;

    private SanKeyboardManager keyboardsManager;

    private boolean isSpacePressed;
    private boolean isDeletePressed;

    private final Timer spaceTimer = new Timer();
    private int spaceKeyIndex = SanKeyboardUtils.INVALID_INDEX;
    private ShiftMode shiftMode = ShiftMode.LOWER_CASE;
    private InputLanguage inputLanguage;
    private TimerTask spaceLongPressTask;
    private boolean animating = false;

    private BaseSecureEditText secureEditText;
    private InputConnection inputConnection;
    private SanEventCallbacks eventListener;

    private AnimationsManager animationsManager;
    private SpaceKeyStateManager spaceKeyStateManager;

    private SanKeyboardCallback sanKeyboardCallback;

    // For controlling Alternate Pop Up Chars visibility
    private boolean isLongPressDownDetected;
    private boolean isLongPressUpDetected;

    private boolean isLanguageSelectorShown;

    // For intercepting OnEditorActionListener
    private TextView.OnEditorActionListener editorActionListener;

    // CONSTRUCTORS
    // ***********************************************************************

    public SanKeyboardView(@NonNull Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SanKeyboardView(@NonNull Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SanKeyboardView(@NonNull Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);

        setFilterTouchesWhenObscured(true);
        setOnKeyboardActionListener(this);

        initLanguages();

        checkIfAntiTapJackingIsSetted();

        isLongPressDownDetected = false;
        isLongPressUpDetected = false;

    }

    private void initLanguages() {

        // Get possible languages from constructor or other option
        inputLanguage = InputLanguage.forLocale(getContext().getResources().getConfiguration().locale);
        InputLanguage deviceLanguage = InputLanguage.forLocale(getContext().getResources().getConfiguration().locale);
        this.inputLanguage = InputLanguage.defaultLanguage;

        for (InputLanguage possibleLanguage : SanKeyboardManager.getPossibleLanguages()) {
            if (possibleLanguage == deviceLanguage) {
                this.inputLanguage = deviceLanguage;//Only if the language of the device is in the list of allowed!
                break;
            }
        }

        keyboardsManager = new SanKeyboardManager(SanKeyboardUtils.getContextForLocale(getContext(), inputLanguage.getLocale()));

    }

    private void checkIfAntiTapJackingIsSetted() {

        if (SanKeyboardManager.getSanTapJackedCallback() == null) {
            throw new IllegalStateException(
                    "SanTapJackedCallback not initialized. In order to use the SanKeyboard you must set SanKeyboardManager"
                            + ".sanTapJackedCallback");
        }

    }

    public void initAnimationsManager() {
        animationsManager = new AnimationsManager(this);
    }

    // GETTERS & SETTERS
    // ***********************************************************************

    @Override
    public SanKeyboard getKeyboard() {
        return (SanKeyboard) super.getKeyboard();
    }

    public void setSanKeyboardCallback(SanKeyboardCallback sanKeyboardCallback) {
        this.sanKeyboardCallback = sanKeyboardCallback;
    }

    @Nullable
    public SanEventCallbacks getEventListener() {
        return eventListener;
    }

    public void setEventListener(SanEventCallbacks eventListener) {
        this.eventListener = eventListener;
    }

    public SanKeyboardType getKeyboardType() {
        return keyboardsManager != null ? keyboardsManager.getKeyboardType() : null;
    }

    public boolean isLanguageSelectorShown() {
        return isLanguageSelectorShown;
    }

    public void setOnEditorActionListener(TextView.OnEditorActionListener listener) {
        editorActionListener = listener;
    }

    // METHODS
    // ***********************************************************************

    // TOUCH EVENTS
    // --------------------------

    @Override
    public boolean onFilterTouchEventForSecurity(MotionEvent event) {

        boolean touchEventDispatched = super.onFilterTouchEventForSecurity(event);

        SanTapJackedCallback sanKeyboardTapJackedCallback = SanKeyboardManager.getSanTapJackedCallback();

        if (!touchEventDispatched && sanKeyboardTapJackedCallback != null) {
            return sanKeyboardTapJackedCallback.onObscuredTouchEvent(event);
        }

        return touchEventDispatched;

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent me) {

        if (isDeletePressed && me.getAction() == MotionEvent.ACTION_UP) {
            isDeletePressed = false;
            KeyboardAOSP.Key deleteKey = getKeyboard().getKeyByCode(KeyboardAOSP.KEYCODE_DELETE);
            if (deleteKey != null) {
                deleteKey.icon = getResources().getDrawable(R.drawable.key_backspace_default);
            }
        }

        if (isSpacePressed && me.getAction() == MotionEvent.ACTION_UP) {
            isSpacePressed = false;
            KeyboardAOSP.Key spaceKey = getKeyboard().getKeyByCode(SanKeyboard.KEYCODE_SPACE);
            if (spaceKey != null && spaceLongPressTask != null) {//onKeyUp not called?!
                spaceLongPressTask.cancel();
            }
        }

        // If a previous LongPress was completed (Down and Up), and this is the next Up movement without clicking
        // any other key before (onKey resets the flags), then maybe there is a Alternate Chars Popup shown on screen.
        if (isLongPressDownDetected && isLongPressUpDetected && me.getAction() == MotionEvent.ACTION_UP) {
            handleBack();   // If there is a popup alternate chars, try to dismiss it
            return true;    // Set event as consumed
        }

        // If a previous LongPress Down was completed but not Up and this is an Up movement, mark
        // Long Press action as complete (Down and Up) for unlock dismissing popup on next touch
        if (isLongPressDownDetected && !isLongPressUpDetected && me.getAction() == MotionEvent.ACTION_UP) {
            isLongPressUpDetected = true;
        }

        return super.onTouchEvent(me); //Cannot check here which key was pressed

    }

    // KEYBOARDVIEW OVERRIDE METHODS
    // --------------------------

    @Override
    public void invalidateKey(int keyIndex) { //Called by onTouchEvent
        // If there's a space key on this keyboard and it has been interacted with.
        if (isSpacePressed || keyIndex == spaceKeyIndex) {
            setupSpaceKey(getKeyboard());
        }
        super.invalidateKey(keyIndex);
    }

    /**
     * {@inheritDoc}
     *
     * @param keyboard Make sure this is an instance of {@link SanKeyboard}.
     */
    @Override
    public void setKeyboard(KeyboardAOSP keyboard) {

        if (!(keyboard instanceof SanKeyboard)) {
            throw new IllegalArgumentException("Keyboard object not an instance of " + SanKeyboard.class.getName());
        }

        setupSpaceKey((SanKeyboard) keyboard);
        super.setKeyboard(keyboard);
        shiftTo(getKeyboard().getInitialShift());

    }


    // GENERAL METHODS
    // --------------------------

    public void setInputConnection(@NonNull BaseSecureEditText editText) {

        this.secureEditText = editText;
        setKeyboard(keyboardsManager.getKeyboardForType(editText.getKeyboardType(), editText.getTopRowButtons()));
        this.inputConnection = editText.onCreateInputConnection(new EditorInfo());

    }

    private void shiftTo(ShiftMode shiftMode) {
        this.shiftMode = shiftMode;
        if (shiftMode != null) {
            setShifted(ShiftMode.LOWER_CASE != shiftMode);
            KeyboardAOSP.Key shiftKey = getKeyboard().getKeyByCode(KeyboardAOSP.KEYCODE_SHIFT);
            if (shiftKey != null) {
                shiftKey.icon = getResources().getDrawable(shiftMode.getDrawableResId());
            }
        }
    }

    private void changeModeTo(SanKeyboard sanKeyboard) {
        setKeyboard(sanKeyboard);
    }


    // SPACE KEY && LANGUAGE DIALOG
    // --------------------------

    private void setupSpaceKey(SanKeyboard keyboard) {

        if (!keyboard.equals(getKeyboard())) {
            spaceKeyIndex = keyboard.getKeyIndexByCode(SanKeyboard.KEYCODE_SPACE);
        }

        if (spaceKeyIndex == SanKeyboardUtils.INVALID_INDEX) {
            return;
        }

        final KeyboardAOSP.Key spaceKey = keyboard.getKeys().get(spaceKeyIndex);

        if (spaceKeyStateManager == null) {
            spaceKeyStateManager = new SpaceKeyStateManager(getContext(), spaceKey.width, spaceKey.height);
            spaceKey.label = null;
        }

        if (spaceKey.pressed) {

            // This is a trick to evade the default key background and use the custom one for the space key.
            isSpacePressed = true;
            spaceKey.pressed = false;
            spaceKey.icon = spaceKeyStateManager.getDrawableForState(inputLanguage, SpaceKeyState.PRESSED);

            // If pressed 3 seconds, launch language change dialog
            if (SanKeyboardManager.getPossibleLanguages().length > 1) {
                if (spaceLongPressTask != null) {
                    spaceLongPressTask.cancel();//Cancel any previous task
                }
                spaceLongPressTask = new TimerTask() {
                    @Override
                    public void run() {
                        if (isSpacePressed) {
                            startLanguageDialog(spaceKey);
                        }
                    }
                };
                spaceTimer.schedule(spaceLongPressTask, CHANGE_LANGUAGE_WAIT);
            }

        } else {

            spaceKey.icon = spaceKeyStateManager.getDrawableForState(inputLanguage, SpaceKeyState.NORMAL);

            if (isSpacePressed) {
                isSpacePressed = false;
                invalidateKey(spaceKeyIndex);
            }

        }

    }

    private void startLanguageDialog(KeyboardAOSP.Key spaceKey) {

        final Activity activity = getActivity();
        spaceKey.icon = spaceKeyStateManager.getDrawableForState(inputLanguage, SpaceKeyState.NORMAL);

        isSpacePressed = false;

        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    invalidateKey(spaceKeyIndex);

                    isLanguageSelectorShown = true;

                    SelectLanguageDialog.showSelectLanguageDialog(activity,
                            keyboardsManager.getCurrentLanguage(),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    onLanguageSelected(dialog);

                                    isLanguageSelectorShown = false;

                                    secureEditText.retainFocus();

                                }
                            },
                            new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    undoInsertSpace();

                                    isLanguageSelectorShown = false;

                                }
                            });

                }

            });
        }

    }

    private void undoInsertSpace() {

        if (!TextUtils.isEmpty(secureEditText.getText())) {
            final String pre = secureEditText.getText().toString();
            secureEditText.setText(pre.substring(0, pre.length() - 1));
        }

    }

    private void onLanguageSelected(DialogInterface dialog) {

        ListView lw = ((AlertDialog) dialog).getListView();
        int lwCheckedItemPosition = lw.getCheckedItemPosition();

        InputLanguage currentLanguage = InputLanguage.values()[lwCheckedItemPosition];
        SanKeyboardView.this.inputLanguage = currentLanguage;
        keyboardsManager.updateKeyboards(currentLanguage);
        changeModeTo(keyboardsManager.getKeyboardForType(keyboardsManager.getKeyboardType(),
                keyboardsManager.getCurrent().getTopRowButtonsSelected()));

    }


    /**
     * Needed, as this may be created by a non-activity {@link Context}.
     *
     * @return the Activity where this View is shown
     */
    @Nullable
    private Activity getActivity() {

        Context context = getContext();

        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }

        return null;

    }

    // KEYBOARD ACTION EVENTS
    // --------------------------

    @Override
    protected boolean onLongPress(KeyboardAOSP.Key popupKey) {
        isLongPressDownDetected = true;
        return super.onLongPress(popupKey);
    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {

        if (inputConnection == null) {
            return;
        }

        // Reset any LongPress (show Alternate Char PopUp) control process after a key is pressed. If the key comes from
        // the popup keyboard, then this will reset the process for the next time
        isLongPressUpDetected = false;
        isLongPressDownDetected = false;

        SanKeyboardType keyboardType = keyboardsManager.getKeyboardType();
        SanKeyboard current = keyboardsManager.getCurrent();

        TopRowButtonsOptions topRowButtons = TopRowButtonsOptions.NONE;

        if (current != null) {
            topRowButtons = current.getTopRowButtonsSelected();
        }

        if (!getKeyboard().isKeyEnabled(primaryCode)) return;

        switch (primaryCode) {

            case KeyboardAOSP.KEYCODE_SHIFT:
                shiftTo(shiftMode.getNext());
                break;

            case KeyboardAOSP.KEYCODE_MODE_CHANGE://Unused as for now
                switch (keyboardType) {
                    case SPECIAL_CHARACTER:
                    case SPECIAL_CHARACTER_NEXT:
                        changeModeTo(keyboardsManager.getKeyboardForType(SanKeyboardType.ALPHA, topRowButtons));
                        break;
                    default:
                        changeModeTo(
                                keyboardsManager.getKeyboardForType(SanKeyboardType.SPECIAL_CHARACTER, topRowButtons));
                        break;
                }
                break;

            case SanKeyboard.KEYCODE_SPECIAL_CHANGE:
                switch (keyboardType) {
                    case SPECIAL_CHARACTER:
                        changeModeTo(keyboardsManager.getKeyboardForType(SanKeyboardType.SPECIAL_CHARACTER_NEXT,
                                topRowButtons));
                        break;
                    case SPECIAL_CHARACTER_NEXT:
                        changeModeTo(
                                keyboardsManager.getKeyboardForType(SanKeyboardType.SPECIAL_CHARACTER, topRowButtons));
                        break;
                    default:
                        changeModeTo(keyboardsManager.getKeyboardForType(SanKeyboardType.ALPHA, topRowButtons));
                        break;
                }
                break;

            case KeyboardAOSP.KEYCODE_CANCEL:
                hide();
                if (sanKeyboardCallback != null) {
                    sanKeyboardCallback.onCancelClick();
                }
                break;

            case SanKeyboard.KEYCODE_CONTINUE:
                hide();
                if (sanKeyboardCallback != null) {
                    sanKeyboardCallback.onContinueClick();
                }
                inputConnection.performEditorAction(EditorInfo.IME_ACTION_NEXT);
                break;

            case KeyboardAOSP.KEYCODE_DONE:

                if (editorActionListener != null) {
                    editorActionListener.onEditorAction(secureEditText, EditorInfo.IME_ACTION_DONE, null);
                }

                hide();
                break;

            case KeyboardAOSP.KEYCODE_DELETE:

                CharSequence selectedText = inputConnection.getSelectedText(0);
                disableComposerRegion();

                if (TextUtils.isEmpty(selectedText)) {
                    // no selection, so delete previous character
                    inputConnection.deleteSurroundingText(1, 0);

                } else {
                    // delete the selection
                    inputConnection.commitText("", 1);
                }

                break;

            case SanKeyboard.KEYCODE_SECURE_KEYBOARD:
                // Ignore clicks on Secure Keyboard button
                break;

            case SanKeyboard.KEYCODE_DECIMAL_POINT:

                // Ignore "." char if the keyboard is decimal and the edit text already contains one "." character
                if (keyboardType == SanKeyboardType.DECIMAL &&
                        secureEditText.getText() != null && secureEditText.getText().toString().contains(".")) {
                    break;
                } else {
                    processRegularChar(primaryCode);
                }
                break;

            default:

                processRegularChar(primaryCode);
                break;

        }

    }

    private void processRegularChar(int primaryCode) {

        String character = String.valueOf((char) primaryCode);
        if (isShifted()) {
            character = character.toUpperCase();
        }

        disableComposerRegion();
        inputConnection.commitText(character, 1);

        if (ShiftMode.UPPER_CASE_SINGLE == shiftMode) {
            shiftTo(ShiftMode.LOWER_CASE);
        }

    }

    private void disableComposerRegion(){

        // Needed to avoid problems in some devices that automatically starts typing in Composer mode, making the text
        // being constantly replaced by the char inserted instead of keeping full text typed.
        // When T9-Predictor it's going to be introduced, we must deal with this topic
        inputConnection.setComposingRegion(0,0);

    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onPress(int primaryCode) {

        if (primaryCode == KeyboardAOSP.KEYCODE_DELETE) {
            isDeletePressed = true;
            getKeyboard().getKeyByCode(KeyboardAOSP.KEYCODE_DELETE).icon =
                    getResources().getDrawable(R.drawable.key_backspace_pressed);
        }

    }

    @Override
    public void onRelease(int primaryCode) {
        // Nothing to do...
    }

    @Override
    public void onText(CharSequence text) {
        // Nothing to do...
    }

    @Override
    public void swipeLeft() {
        // Nothing to do...
    }

    @Override
    public void swipeRight() {
        // Nothing to do...
    }

    @Override
    public void swipeDown() {
        // Nothing to do...
    }

    @Override
    public void swipeUp() {
        // Nothing to do...
    }


    // ANIMATIONS
    // --------------------------

    public void slideIn() {
        startAnimation(animationsManager.getSlideInAnimation());
    }

    public void slideOut() {
        startAnimation(animationsManager.getSlideOutAnimation());
    }

    private void hide() {
        secureEditText.clearAutoScrollExtraRequests();
        slideOut();
        secureEditText.clearFocus();

    }

    @Override
    protected void onAnimationStart() {
        super.onAnimationStart();
        animating = true;
    }

    @Override
    protected void onAnimationEnd() {
        super.onAnimationEnd();
        animating = false;
    }

    public boolean isAnimating() {
        return animating;
    }

    @Override
    public boolean handleBack() {

        // When handlingBack, we have to reset flags for making the popup dismissable for the next time
        isLongPressUpDetected = false;
        isLongPressDownDetected = false;

        return super.handleBack();

    }

    public void onDestroyView(){

        secureEditText = null;
        sanKeyboardCallback = null;
        inputConnection = null;

    }


}
