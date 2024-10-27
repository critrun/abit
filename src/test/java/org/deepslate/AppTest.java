package org.deepslate;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.deepslate.abit.ABITArray;
import org.deepslate.abit.ABITException;
import org.deepslate.abit.ABITObject;
import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public void buildTree() throws ABITException, IOException
    {
        ABITObject tree = new ABITObject();

        tree.put("null obj", ABITObject.NULL);
        tree.put("boolean t obj", true);
        tree.put("boolean f obj", false);
        tree.put("integer p big", 69696969420L);
        tree.put("integer n big", -69696969420L);
        tree.put("integer p small", 69L);
        tree.put("integer n small", -69L);
        byte[] blobs = {0,1,2,3,4,5,6,7};
        byte[] blobb = new byte[128];
        tree.put("small blob obj", blobs);
        tree.put("big blob obj", blobb);
        tree.put("string small", "Hello 💀");
        tree.put("string big", "Lorem ipsum dolor sit 💺🧘‍♂️ amet, consectetur adipiscing elit, sed do 👌 eiusmod tempor incididunt ut 🅱️🤫 labore et 🎻📯🎺 dolore magna aliqua. Ut 🅱️🤫 enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut 🅱️🤫 aliquip ex 🎻🎻🎻🎻🎻🎻 ea commodo consequat. Duis aute irure dolor in 😩 reprehenderit in 🍝🥫 voluptate velit esse cillum dolore eu 🤲 fugiat nulla pariatur. Excepteur sint occaecat cupidatat non ❌ proident, sunt in 😩😂 culpa qui officia deserunt mollit anim id 😗 est ⏩💆👷🏿 laborum.");
        ABITArray arr = new ABITArray();
        arr.add("1");
        arr.add(2L);
        arr.add(true);
        arr.add(4L);
        arr.add(3L);
        arr.add("2");
        arr.add(1L);
        tree.put("array obj", arr);
        ABITObject nestedTree = new ABITObject();
        nestedTree.put("thing", "AMOGUS");
        tree.put("nesty", nestedTree);
        tree.put("a very very very very very very very very very very very very very  very very very very very very very very very very very very very  very very very very very very very very very very very very very  very very very very very very very very very ve long key", "meow");

        byte[] treeblob1 = tree.toByteArray();
        byte[] treeblob2 = (new ABITObject(treeblob1)).toByteArray();

        assertTrue(treeblob1.length == treeblob2.length);
        for (int i = 0; i < treeblob1.length; i++) {
            assertTrue(treeblob1[i] == treeblob2[i]);
        }

        System.out.println(tree.getJson().toString(2));

        System.out.println("ABIT Size: "+treeblob1.length+" JSON Size: "+tree.getJson().toString().getBytes(StandardCharsets.UTF_8).length);

    }
}
