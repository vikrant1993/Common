package vk.help.views;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;

import com.google.android.material.textfield.TextInputLayout;

public class MaterialEditText extends TextInputLayout {

    private AppCompatEditText editText;
    private Context context;

    public MaterialEditText(@NonNull Context context) {
        super(context);
        this.context = context;
        init();
    }

    public MaterialEditText(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public MaterialEditText(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        editText = new AppCompatEditText(context);
        addView(editText);
    }
}