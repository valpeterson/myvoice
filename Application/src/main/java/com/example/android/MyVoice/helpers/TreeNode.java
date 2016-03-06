package com.example.android.MyVoice.helpers;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Val on 2/12/2016.
 */
public class TreeNode<SpeechItem> {

    SpeechItem data;
    TreeNode<SpeechItem> parent;
    List<TreeNode<SpeechItem>> children;

    public TreeNode(SpeechItem data) {
        this.data = data;
        this.children = new LinkedList<TreeNode<SpeechItem>>();
    }

    public TreeNode<SpeechItem> addChild(SpeechItem child) {
        TreeNode<SpeechItem> childNode = new TreeNode<SpeechItem>(child);
        childNode.parent = this;
        this.children.add(childNode);
        return childNode;
    }

    public int getNumChildren() {
        return this.children.size();
    }

    public TreeNode getChild(int position) {
        if (position < children.size()) {
            return children.get(position);
        } else {
            return null;
        }
    }

    public String toString() {
        return data.toString();
    }

}
