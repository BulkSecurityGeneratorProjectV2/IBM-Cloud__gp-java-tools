/*
 * Copyright IBM Corp. 2016, 2018
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ibm.g11n.pipeline.resfilter.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.junit.Test;

import com.ibm.g11n.pipeline.resfilter.FilterOptions;
import com.ibm.g11n.pipeline.resfilter.LanguageBundle;
import com.ibm.g11n.pipeline.resfilter.LanguageBundleBuilder;
import com.ibm.g11n.pipeline.resfilter.ResourceFilterException;
import com.ibm.g11n.pipeline.resfilter.ResourceString;
import com.ibm.g11n.pipeline.resfilter.ResourceString.ResourceStringComparator;

/**
 * @author Farhan Arshad
 *
 */
public class IOSStringsResourceTest {
    private static final File INPUT = new File("src/test/resource/resfilter/ios/input.strings");

    private static final File EXPECTED_WRITE_FILE = new File("src/test/resource/resfilter/ios/write-output.strings");

    private static final File MERGE_INPUT_1_FILE = new File("src/test/resource/resfilter/ios/merge-input-1.strings");
    private static final File MERGE_INPUT_2_FILE = new File("src/test/resource/resfilter/ios/merge-input-2.strings");
    private static final File EXPECTED_MERGE_1_FILE = new File("src/test/resource/resfilter/ios/merge-output-1.strings");
    private static final File EXPECTED_MERGE_2_FILE = new File("src/test/resource/resfilter/ios/merge-output-2.strings");

    private static final Collection<ResourceString> EXPECTED_INPUT_RES_LIST;
    private static final List<String> EXPECTED_GLOBAL_NOTES = Arrays.asList(
            " This is the first global comment... ",
            " This is the 2nd global comment... ");

    static {
        List<ResourceString> lst = new ArrayList<>();
        lst.add(ResourceString.with("Insert Element", "Insert Element").sequenceNumber(1)
                .notes(Arrays.asList(" Insert = Element menu item ")).build());
        lst.add(ResourceString.with("ErrorString_1", "An unknown error occurred.").sequenceNumber(2)
                .notes(Arrays.asList(" Error string used ","    for unknown error types. ")).build());
        lst.add(ResourceString.with("bear 3", "Brown Bear").sequenceNumber(3)
                .notes(Arrays.asList(" Brown Bear has multiple comments "," Mama Bear ", " Papa Bear "," Baby Bear ")).build());
        lst.add(ResourceString.with("frog 4", "Red-eyed Tree Frog").sequenceNumber(4).build());
        lst.add(ResourceString.with("owl 5", "Great Horned Owl").sequenceNumber(5).build());

        Collections.sort(lst, new ResourceStringComparator());
        EXPECTED_INPUT_RES_LIST = lst;
    }

    private static LanguageBundle WRITE_BUNDLE;

    static {
        LanguageBundleBuilder bundleBuilder = new LanguageBundleBuilder(false);
        bundleBuilder.addResourceString("sealion 3", "California Sea Lion", 3,
                Arrays.asList(" This is a brilliant comment","  about California Sea Lions! "));

        bundleBuilder.addResourceString("otter 1", "Sea Otter", 1,
                Arrays.asList(" The sea otter swims a lot. "));
        bundleBuilder.addResourceString("crow 2", "American Crow", 2);
        bundleBuilder.addResourceString("Lorem",
                "Loremのイプサムは、単に印刷と植字業界のダミーテキストです。 Loremのイプサムは、未知のプリンターがタイプのゲラを取り、"
                        + "タイプ標本の本を作ってそれをスクランブル1500年代、以来、業界の標準ダミーテキストとなっています。それは本質的に変わらず、"
                        + "何世紀だけでなく、電子組版に飛躍するだけでなく5を生き延びてきました。"
                        + "それはLoremのイプサムのバージョンを含むアルダスのPageMakerのようなデスクトップパブリッシングソフトウェアと、"
                        + "より最近Loremのイプサムの通路を含むLetrasetシートのリリースでは、1960年代に普及したところ。",
                5);
        bundleBuilder.addResourceString("numbers",
                "1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 "
                        + "1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 "
                        + "1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 ",
                4);
        bundleBuilder.addNote(" This is the first global comment... ");
        bundleBuilder.addNote(" This is the 2nd global comment... ");
        WRITE_BUNDLE = bundleBuilder.build();
    }

    private static LanguageBundle MERGE_BUNDLE;

    static {
        LanguageBundleBuilder bundleBuilder = new LanguageBundleBuilder(false);
        bundleBuilder.addResourceString("Insert Element", "Insert Element - translated", 1);
        bundleBuilder.addResourceString("ErrorString_1", "An unknown error occurred - translated.", 2);
        bundleBuilder.addResourceString("bear 3", "Brown Bear - translated", 3);
        bundleBuilder.addResourceString("frog 4", "Red-eyed Tree Frog - translated", 4);
        bundleBuilder.addResourceString("owl 5", "Great Horned Owl - translated", 5);
        bundleBuilder.addResourceString(
                "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the "
                        + "industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of "
                        + "type and scrambled it to make a type specimen book. It has survived not only five centuries, "
                        + "but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised"
                        + " in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently"
                        + " with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.",
                "Loremのイプサムは、単に印刷と植字業界のダミーテキストです。 Loremのイプサムは、未知のプリンターがタイプのゲラを取り、"
                        + "タイプ標本の本を作ってそれをスクランブル1500年代、以来、業界の標準ダミーテキストとなっています。それは本質的に変わらず、"
                        + "何世紀だけでなく、電子組版に飛躍するだけでなく5を生き延びてきました。"
                        + "それはLoremのイプサムのバージョンを含むアルダスのPageMakerのようなデスクトップパブリッシングソフトウェアと、"
                        + "より最近Loremのイプサムの通路を含むLetrasetシートのリリースでは、1960年代に普及したところ。",
                7);
        bundleBuilder.addResourceString("numbers",
                "1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 "
                        + "1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 "
                        + "1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 ",
                6);
        MERGE_BUNDLE = bundleBuilder.build();
    }

    private static final IOSStringsResource res = new IOSStringsResource();

    @Test
    public void testParse() throws IOException, ResourceFilterException {
        assertTrue("The input test file <" + INPUT + "> does not exist.", INPUT.exists());

        try (InputStream is = new FileInputStream(INPUT)) {
            LanguageBundle bundle = res.parse(is, null);
            List<ResourceString> resStrList = new ArrayList<>(bundle.getResourceStrings());
            Collections.sort(resStrList, new ResourceStringComparator());
            assertEquals("ResourceStrings did not match.", EXPECTED_INPUT_RES_LIST, resStrList);
            List<String> globalNotes = bundle.getNotes();
            assertEquals("Global comments did not match.", EXPECTED_GLOBAL_NOTES, globalNotes);
        }
    }

    @Test
    public void testWrite() throws IOException, ResourceFilterException {
        File tempFile = Files.createTempFile(this.getClass().getSimpleName(), ".strings").toFile();
        tempFile.deleteOnExit();

        try (OutputStream os = new FileOutputStream(tempFile)) {
            res.write(os, WRITE_BUNDLE, null);
            os.flush();
            assertTrue(ResourceTestUtil.compareFiles(EXPECTED_WRITE_FILE, tempFile));
        }
    }

    @Test
    public void testMerge() throws IOException, ResourceFilterException {
        File tempFile;

        tempFile = Files.createTempFile(this.getClass().getSimpleName(), ".strings").toFile();
        tempFile.deleteOnExit();

        try (OutputStream os = new FileOutputStream(tempFile);
                InputStream is = new FileInputStream(MERGE_INPUT_1_FILE)) {
            res.merge(is, os, MERGE_BUNDLE, new FilterOptions(Locale.ENGLISH));
            os.flush();
            assertTrue(ResourceTestUtil.compareFiles(EXPECTED_MERGE_1_FILE, tempFile));
        }

        tempFile = Files.createTempFile(this.getClass().getSimpleName(), ".strings").toFile();
        tempFile.deleteOnExit();

        try (OutputStream os = new FileOutputStream(tempFile);
                InputStream is = new FileInputStream(MERGE_INPUT_2_FILE)) {
            res.merge(is, os, MERGE_BUNDLE, new FilterOptions(Locale.JAPANESE));
            os.flush();
            assertTrue(ResourceTestUtil.compareFiles(EXPECTED_MERGE_2_FILE, tempFile));
        }
    }
}
