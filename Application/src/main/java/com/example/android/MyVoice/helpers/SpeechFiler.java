package com.example.android.MyVoice.helpers;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.IllegalArgumentException;
import java.util.HashMap;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

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

    static final String CATALOG_FILENAME = "myvoice_speech_catalog_sample.xml";
    // names of the XML tags
    static final String XML_ROOT = "myvoice";
    static final String XML_CATEGORY = "category";
    static final String XML_PHRASE = "phrase";
    static final String XML_NAME = "name";
    static final String XML_FILE = "speechFile";
    static final String XML_ORDER = "order";

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

    void LoadCatalog() throws XmlPullParserException, IOException {
        SpeechItem curItem;
        String label;
        String speechFile;
        BufferedReader xmlReader;
        String tempLine;
        TreeNode childNode;
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();
        String name;

        Log.e(TAG, "Trying to read speech catalog: " + basePath + CATALOG_FILENAME);
        File xmlFile = new File(basePath, CATALOG_FILENAME);
        if (xmlFile.exists()) {
            Log.e(TAG, "Found XML file");
        } else {
            Log.e(TAG, "Cannot find " + basePath + CATALOG_FILENAME);
        }

        xpp.setInput(new FileInputStream(xmlFile), null);
        int eventType = xpp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            name = null;
            switch (eventType) {
                case XmlPullParser.START_DOCUMENT:
                    break;
                case XmlPullParser.START_TAG:
                    name = xpp.getName();
                    Log.e(TAG, "Start tag " + name);
                    if (name.equalsIgnoreCase(XML_PHRASE)) {
                        label = xpp.getAttributeValue(null, XML_NAME);
                        speechFile = xpp.getAttributeValue(null, XML_FILE);
                        curItem = new SpeechItem(String.valueOf(nextID), label, speechFile, false);
                        childNode = currentNode.addChild(curItem);
                        ITEM_MAP.put(String.valueOf(nextID), curItem);
                        nextID++;
                    } else if (name.equalsIgnoreCase(XML_CATEGORY)) {
                        label = xpp.getAttributeValue(null, XML_NAME);
                        speechFile = xpp.getAttributeValue(null, XML_FILE);
                        curItem = new SpeechItem(String.valueOf(nextID), label, speechFile, true);
                        childNode = currentNode.addChild(curItem);
                        ITEM_MAP.put(String.valueOf(nextID), curItem);
                        nextID++;
                        parentNode = currentNode;
                        currentNode = childNode;    //we're going to now add children of this next-level directory
                    } else {
                        Log.e(TAG, "Encountered unexpected XML tag, " + name + ", in speech catalog");
                    }
                    break;
                case XmlPullParser.END_TAG:
                    name = xpp.getName();
                    Log.e(TAG, "End tag " + name);
                    if (name.equalsIgnoreCase(XML_CATEGORY)) {
                        currentNode = parentNode;   //go back to adding children of the node we were on before
                    }
                    break;
            }
            try {
                eventType = xpp.next();
            } catch (XmlPullParserException e) {
                Log.e(TAG, "XML Pull Parser Exception:");
                Log.e(TAG, "  LINE: " + e.getLineNumber());
                Log.e(TAG, "  COLUMN: " + e.getColumnNumber());
                Log.e(TAG, "  DETAIL: " + e.getDetail());
                throw e;
            }
        }
    }

/*
    private void LoadDirectory(File currentDir) throws XmlPullParserException, IOException {
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
*/

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
