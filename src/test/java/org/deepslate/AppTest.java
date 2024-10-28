package org.deepslate;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.deepslate.abit.ABITArray;
import org.deepslate.abit.ABITException;
import org.deepslate.abit.ABITObject;
import java.util.Random;
import org.junit.Test;

public class AppTest 
{
    /**
     * Rigorous Test :-)
     */
    public boolean compareArray(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        }
        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }

    @Test
    public void treePutNull() throws Exception {
        ABITObject tree = new ABITObject();
        ABITObject.NULL_t obj = ABITObject.NULL;

        // Legal keys
        tree.put("null obj", ABITObject.NULL);
        tree.put(" ".repeat(128), ABITObject.NULL);
        tree.put(" ".repeat(129), ABITObject.NULL);
        tree.put(" ".repeat(255), ABITObject.NULL);
        tree.put(" ".repeat(256), ABITObject.NULL);
        // Illegal keys
        try {
            tree.put(" ".repeat(257), ABITObject.NULL);
            throw new Exception("This shouldn't succeed");
        }
        catch (IllegalArgumentException e) {}
        try {
            tree.put(" ".repeat(6969), ABITObject.NULL);
            throw new Exception("This shouldn't succeed");
        }
        catch (IllegalArgumentException e) {}
        try {
            tree.put("", ABITObject.NULL);
            throw new Exception("This shouldn't succeed");
        }
        catch (IllegalArgumentException e) {}
    }

    @Test
    public void treePutBoolean() throws Exception {
        ABITObject tree = new ABITObject();
        boolean obj = true;

        // Legal keys
        tree.put("null obj", obj);
        tree.put(" ".repeat(128), obj);
        tree.put(" ".repeat(129), obj);
        tree.put(" ".repeat(255), obj);
        tree.put(" ".repeat(256), obj);
        // Illegal keys
        try {
            tree.put(" ".repeat(257), obj);
            throw new Exception("This shouldn't succeed");
        }
        catch (IllegalArgumentException e) {}
        try {
            tree.put(" ".repeat(6969), obj);
            throw new Exception("This shouldn't succeed");
        }
        catch (IllegalArgumentException e) {}
        try {
            tree.put("", obj);
            throw new Exception("This shouldn't succeed");
        }
        catch (IllegalArgumentException e) {}

        obj = false;

        // Legal keys
        tree.put("bool obj", obj);
        tree.put(" ".repeat(128), obj);
        tree.put(" ".repeat(129), obj);
        tree.put(" ".repeat(255), obj);
        tree.put(" ".repeat(256), obj);
        // Illegal keys
        try {
            tree.put(" ".repeat(257), obj);
            throw new Exception("This shouldn't succeed");
        }
        catch (IllegalArgumentException e) {}
        try {
            tree.put(" ".repeat(6969), obj);
            throw new Exception("This shouldn't succeed");
        }
        catch (IllegalArgumentException e) {}
        try {
            tree.put("", obj);
            throw new Exception("This shouldn't succeed");
        }
        catch (IllegalArgumentException e) {}
    }

    @Test
    public void treePutInteger() throws Exception {
        ABITObject tree = new ABITObject();
        long obj = 6969696969420L;

        // Legal keys
        tree.put("int obj", obj);
        assertTrue(obj == (new ABITObject(tree.toByteArray())).getInteger("int obj"));
        tree.put(" ".repeat(128), obj);
        assertTrue(obj == (new ABITObject(tree.toByteArray())).getInteger(" ".repeat(128)));
        tree.put(" ".repeat(129), obj);
        assertTrue(obj == (new ABITObject(tree.toByteArray())).getInteger(" ".repeat(129)));
        tree.put(" ".repeat(255), obj);
        assertTrue(obj == (new ABITObject(tree.toByteArray())).getInteger(" ".repeat(255)));
        tree.put(" ".repeat(256), obj);
        assertTrue(obj == (new ABITObject(tree.toByteArray())).getInteger(" ".repeat(256)));
        // Illegal keys
        try {
            tree.put(" ".repeat(257), obj);
            throw new Exception("This shouldn't succeed");
        }
        catch (IllegalArgumentException e) {}
        try {
            tree.put(" ".repeat(6969), obj);
            throw new Exception("This shouldn't succeed");
        }
        catch (IllegalArgumentException e) {}
        try {
            tree.put("", obj);
            throw new Exception("This shouldn't succeed");
        }
        catch (IllegalArgumentException e) {}

        Random random = new Random();
        for (int i = 0; i < 10000; i++) {
            obj = random.nextLong();
            tree.put("int obj", obj);
            tree.put("meow", obj+5);
            tree.put("meowmeow", -obj);
            assertTrue(obj == (new ABITObject(tree.toByteArray())).getInteger("int obj"));
        }
    }

    @Test
    public void treePutBlob() throws Exception {
        ABITObject tree = new ABITObject();
        Random random = new Random();
        byte[] obj = new byte[0];

        for (int i = 0; i < 5000; i++) {
            obj = new byte[random.nextInt(2)*i];

            // Legal keys
            random.nextBytes(obj);
            tree.put("int obj", obj);
            assertTrue(compareArray(obj, (new ABITObject(tree.toByteArray())).getBlob("int obj")));
            random.nextBytes(obj);
            tree.put(" ".repeat(128), obj);
            assertTrue(compareArray(obj, (new ABITObject(tree.toByteArray())).getBlob(" ".repeat(128))));
            random.nextBytes(obj);
            tree.put(" ".repeat(129), obj);
            assertTrue(compareArray(obj, (new ABITObject(tree.toByteArray())).getBlob(" ".repeat(129))));
            random.nextBytes(obj);
            tree.put(" ".repeat(255), obj);
            assertTrue(compareArray(obj, (new ABITObject(tree.toByteArray())).getBlob(" ".repeat(255))));
            random.nextBytes(obj);
            tree.put(" ".repeat(256), obj);
            assertTrue(compareArray(obj, (new ABITObject(tree.toByteArray())).getBlob(" ".repeat(256))));
            // Illegal keys
            try {
                random.nextBytes(obj);
                tree.put(" ".repeat(257), obj);
                throw new Exception("This shouldn't succeed");
            }
            catch (IllegalArgumentException e) {}
            try {
                random.nextBytes(obj);
                tree.put(" ".repeat(6969), obj);
                throw new Exception("This shouldn't succeed");
            }
            catch (IllegalArgumentException e) {}
            try {
                random.nextBytes(obj);
                tree.put("", obj);
                throw new Exception("This shouldn't succeed");
            }
            catch (IllegalArgumentException e) {}
        }
    }

    @Test
    public void treePutString() throws Exception {
        ABITObject tree = new ABITObject();
        Random random = new Random();
        byte[] rbytes;
        String obj;

        for (int i = 0; i < 5000; i++) {
            rbytes = new byte[random.nextInt(2)*i];

            // Legal keys
            random.nextBytes(rbytes);
            obj = new String(rbytes, StandardCharsets.UTF_8);
            tree.put("int obj", obj);
            assertTrue(obj.equals((new ABITObject(tree.toByteArray())).getString("int obj")));
            random.nextBytes(rbytes);
            obj = new String(rbytes, StandardCharsets.UTF_8);
            tree.put(" ".repeat(128), obj);
            assertTrue(obj.equals((new ABITObject(tree.toByteArray())).getString(" ".repeat(128))));
            random.nextBytes(rbytes);
            obj = new String(rbytes, StandardCharsets.UTF_8);
            tree.put(" ".repeat(129), obj);
            assertTrue(obj.equals((new ABITObject(tree.toByteArray())).getString(" ".repeat(129))));
            random.nextBytes(rbytes);
            obj = new String(rbytes, StandardCharsets.UTF_8);
            tree.put(" ".repeat(255), obj);
            assertTrue(obj.equals((new ABITObject(tree.toByteArray())).getString(" ".repeat(255))));
            random.nextBytes(rbytes);
            obj = new String(rbytes, StandardCharsets.UTF_8);
            tree.put(" ".repeat(256), obj);
            assertTrue(obj.equals((new ABITObject(tree.toByteArray())).getString(" ".repeat(256))));
            // Illegal keys
            try {
                random.nextBytes(rbytes);
                obj = new String(rbytes, StandardCharsets.UTF_8);
                tree.put(" ".repeat(257), obj);
                throw new Exception("This shouldn't succeed");
            }
            catch (IllegalArgumentException e) {}
            try {
                random.nextBytes(rbytes);
                obj = new String(rbytes, StandardCharsets.UTF_8);
                tree.put(" ".repeat(6969), obj);
                throw new Exception("This shouldn't succeed");
            }
            catch (IllegalArgumentException e) {}
            try {
                random.nextBytes(rbytes);
                obj = new String(rbytes, StandardCharsets.UTF_8);
                tree.put("", obj);
                throw new Exception("This shouldn't succeed");
            }
            catch (IllegalArgumentException e) {}
        }
    }

    @Test
    public void treeGenericTest() throws Exception {
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
