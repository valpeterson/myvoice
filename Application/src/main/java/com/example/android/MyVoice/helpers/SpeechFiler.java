package com.example.android.MyVoice.helpers;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.IllegalArgumentException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Val on 1/25/2016.
 */

public class SpeechFiler {
    public static final String TAG = "MyVoice.SpeechFiler";
    private String basePath;
    private static TreeNode speechTree;
    private static int nextID;
    TreeNode currentNode;
    TreeNode parentNode;
    private static Map<String, SpeechItem> ITEM_MAP =
            new HashMap<String, SpeechItem>();

    public SpeechFiler() throws IOException {
        nextID = 0;
        String recordingsPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        recordingsPath += "/myvoice/speech/";
        File filePath = new File(recordingsPath);
        if (!filePath.isDirectory()) {
            filePath.mkdirs();
            if (!filePath.isDirectory()) {
                Log.e(TAG, "Could not create directory '" + recordingsPath + "' for speech files.");
                throw new IOException("Could not create speech files directory");
            }
        }
        basePath = recordingsPath;
        speechTree = new TreeNode<SpeechItem>(null);    //root of tree, no speech item associated with it
        currentNode = speechTree;
    }

    private void LoadDirectory(File currentDir) {
        File labelFile;
        BufferedReader myReader;
        SpeechItem curItem;
        String label;
        File[] dirChildren;
        TreeNode childNode;
        boolean isCategory;

        dirChildren = currentDir.listFiles();
        for (File nextFile : dirChildren) {
            if (nextFile.isDirectory()) {
                labelFile = new File(nextFile.getPath(), "category");
                isCategory = true;
            } else {
                if (nextFile.getName().equals("category")) continue;
                labelFile = new File(nextFile.getParent(), nextFile.getName() + ".label");
                isCategory = false;
            }
            if (labelFile.exists()) {
                try {
                    myReader = new BufferedReader(new FileReader(labelFile.getPath()));
                    label = myReader.readLine();
                } catch (IOException e) {
                    Log.e(TAG, "Unable to open label file, " + labelFile.getName() + ", for " + nextFile.getName() + ". Will not add to list.");
                    continue;
                }
                curItem = new SpeechItem(String.valueOf(nextID), label, nextFile.getAbsolutePath(), isCategory);
                childNode = currentNode.addChild(curItem);
                ITEM_MAP.put(String.valueOf(nextID), curItem);
                nextID++;
            } else {
                Log.e(TAG, "Unable to find label file, " + labelFile.getName() + ", for " + nextFile.getName() + ". Will not add to list.");
                continue;
            }
            if (isCategory) {
                parentNode = currentNode;
                currentNode = childNode;    //we're going to now add children of this next-level directory
                LoadDirectory(nextFile);    //load this next-level directory's contents
                currentNode = parentNode;   //go back to adding children of the node we were on
            }
        }
    }

    public void LoadDictionary() {
        File currentFile;

        currentFile = new File(basePath);
        if (!currentFile.isDirectory()) {
            throw new IllegalArgumentException("base path given for speech files is not a directory");
        }
        LoadDirectory(currentFile);
    }

    public int getNumNodeChildren() {
        return currentNode.getNumChildren();
    }

    public TreeNode getChild(int position) {
        if (currentNode.getNumChildren() >= position + 1) {
            return currentNode.getChild(position);
        } else {
            return null;
        }
    }

    public void selectChild(int position) {
        parentNode = currentNode;
        currentNode = currentNode.getChild(position);
    }

    public void selectParent() {
        currentNode = parentNode;
    }

    public boolean isCategory(){
        return ((SpeechItem)currentNode.data).isCategory;
    }

    public TreeNode getTree() {
        return speechTree;
    }

    public SpeechItem getItemFromID(String id) {
        return ITEM_MAP.get(id);
    }
}
