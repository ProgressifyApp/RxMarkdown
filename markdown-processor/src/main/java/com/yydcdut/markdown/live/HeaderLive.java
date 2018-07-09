/*
 * Copyright (C) 2016 yydcdut (yuyidong2015@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.yydcdut.markdown.live;

import android.text.Editable;
import android.text.style.RelativeSizeSpan;

import com.yydcdut.markdown.syntax.Syntax;
import com.yydcdut.markdown.syntax.SyntaxKey;
import com.yydcdut.markdown.syntax.edit.EditFactory;
import com.yydcdut.markdown.utils.SyntaxUtils;
import com.yydcdut.markdown.utils.TextHelper;

import java.util.List;

/**
 * RxMDEditText, header controller.
 * <p>
 * Created by yuyidong on 16/7/21.
 */
class HeaderLive extends EditLive {

    @Override
    public void beforeTextChanged(CharSequence s, int start, int before, int after) {
        super.beforeTextChanged(s, start, before, after);
        if (before == 0 || mMarkdownConfiguration == null) {
            return;
        }
        String deleteString = s.subSequence(TextHelper.safePosition(start, s), TextHelper.safePosition(start + before, s)).toString();
        String beforeString = null;
        String afterString = null;
        if (start > 0) {
            beforeString = s.subSequence(TextHelper.safePosition(start - 1, s), TextHelper.safePosition(start, s)).toString();
        }
        if (start + before + 1 <= s.length()) {
            afterString = s.subSequence(TextHelper.safePosition(start + before, s), TextHelper.safePosition(start + before + 1, s)).toString();
        }
        //#12# ss(##12 ss) --> ## ss
        if (TextHelper.isNeedFormat(SyntaxKey.KEY_HEADER_SINGLE, deleteString, beforeString, afterString)) {
            shouldFormat = true;
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int after) {
        if (mMarkdownConfiguration == null && !(s instanceof Editable)) {
            return;
        }
        if (shouldFormat) {
            format((Editable) s, start);
            return;
        }
        if (after == 0) {
            return;
        }
        String addString = s.subSequence(TextHelper.safePosition(start, s), TextHelper.safePosition(start + after, s)).toString();
        String beforeString = null;
        String afterString = null;
        if (start + 1 <= s.length()) {
            afterString = s.subSequence(TextHelper.safePosition(start, s), TextHelper.safePosition(start + 1, s)).toString();
        }
        if (start > 0) {
            beforeString = s.subSequence(TextHelper.safePosition(start - 1, s), TextHelper.safePosition(start, s)).toString();
        }
        //## ss --> #12# ss(##12 ss)
        if (TextHelper.isNeedFormat(SyntaxKey.KEY_HEADER_SINGLE, addString, beforeString, afterString)) {
            format((Editable) s, start);
        } else {
            int lineFirstCharPosition = TextHelper.findBeforeNewLineChar(s, start);
            if (s.subSequence(TextHelper.safePosition(lineFirstCharPosition + 1, s), TextHelper.safePosition(lineFirstCharPosition + 2, s)).toString().equals(SyntaxKey.KEY_HEADER_SINGLE)) {
                format((Editable) s, lineFirstCharPosition + 1);
            }
        }
    }

    private void format(Editable editable, int start) {
        SyntaxUtils.removeSpans(editable, start, RelativeSizeSpan.class);
        Syntax syntax = EditFactory.create().getHeaderSyntax(mMarkdownConfiguration);
        List<EditToken> editTokenList = SyntaxUtils.getMatchedEditTokenList(editable, syntax.format(editable), start);
        SyntaxUtils.setSpans(editable, editTokenList);
    }
}
