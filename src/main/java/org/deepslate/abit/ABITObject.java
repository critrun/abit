package org.deepslate.abit;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import java.math.BigInteger;
import io.ipfs.multibase.Multibase;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.ArrayList;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Comparator;

// Anton's Binary Information Tree

public class ABITObject {

    public static class NULL_t {}
    public static NULL_t NULL;

    SortedMap<String, ABITObject> tree = new TreeMap<>();
    List<ABITObject> array = new ArrayList<>();
    byte type = 6;
    boolean booleanValue = true;
    String string;
    byte[] blob;
    long intValue;

    /**
     * Initialize an ABITObject.
     */
    public ABITObject() {
        this.type = 6;
        this.tree.clear();
    }

    /**
     * Initialize an ABITObject from an abit document inside a byte array.
     * @param document the byte array containing the abit document.
     * @throws ABITException
     */
    public ABITObject(byte[] document) throws ABITException {
        this.type = 6;
        this.tree = decodeTree(document, 0, document.length);
    }

    public ABITObject(JSONObject json, String binaryRegex) throws ABITException, IllegalStateException {
        this.type = 6;
        this.tree.clear();

        for(String key: json.keySet()) {
            Object obj = json.get(key);
            switch (obj) {
                case Boolean b -> {
                    this.put(key, b);
                }
                case Integer i -> {
                    this.put(key, i);
                }
                case String s -> {
                    if (key.matches(binaryRegex)) {
                        this.put(key, Multibase.decode(s));
                    }
                    else {
                        this.put(key, s);
                    }
                }
                case JSONArray a -> {
                    this.put(key, new ABITArray(a, binaryRegex, key));
                }
                case JSONObject o -> {
                    this.put(key, new ABITObject(o, binaryRegex));
                }
                default -> {
                    if (json.isNull(key)) {
                        this.put(key, NULL);
                    }
                    else {
                        throw new ABITException("Unsupported object type in json document");
                    }
                }
            }
        }
    }

    ABITObject(SortedMap<String, ABITObject> tree) {
        this.type = 6;
        this.tree = tree;
    }

    ABITObject(List<ABITObject> array) {
        this.type = 5;
        this.array = array;
    }

    ABITObject(String str) {
        this.type = 4;
        this.string = str;
    }

    ABITObject(byte[] blob, boolean isBytes) {
        this.type = 3;
        this.blob = blob.clone();
    }

    ABITObject(long integer) {
        this.type = 2;
        this.intValue = integer;
    }

    /**
     * create a boolean entry
     * @param bool 
     */
    ABITObject(boolean bool) {
        this.type = 1;
        this.booleanValue = bool;
    }

    /**
     * Create a null entry
     */
    ABITObject(NULL_t a) {
        this.type = 0;
    }

    private int decodeKeyLength(byte[] blob, int offset) {
        return (blob[offset]&0xff)+1;
    }

    private String decodeKey(byte[] blob, int offset) {
        int keyLength = decodeKeyLength(blob, offset);

        return new String(blob, offset+1, keyLength, StandardCharsets.UTF_8);
    }

    private int decodeMetadataLength(byte[] blob, int offset) {
        return ((blob[offset] & 0xf0) >> 4) + 1;
    }

    private int decodeType(byte[] blob, int offset) {
        return blob[offset] & 0x0f;
    }

    private void decodeNull(byte[] blob, int offset) throws ABITException {
        if (blob[offset] == 0x00) {
            return;
        }
        else {
            throw new ABITException("Byte at "+offset+" is not a null");
        }
    }

    private boolean decodeBoolean(byte[] blob, int offset) throws ABITException {
        switch (blob[offset]) {
            case 0b00000001:
                return false;
            case 0b00010001:
                return true;
            default:
                throw new ABITException("Byte at "+offset+" is not a boolean");
        }
    }

    private long decodeInteger(byte[] blob, int offset) throws ABITException {
        return decodeInteger(blob, offset, 8);
    }

    private long decodeInteger(byte[] blob, int offset, int maxSize) throws ABITException {
        int size = decodeMetadataLength(blob, offset);

        if (size > maxSize) {
            throw new ABITException("Invalid integer size at "+offset);
        }

        long value = 0;

        // Convert the byte array to a long
        for (int i = 0; i < size; i++) {
            value <<= 8;
            value |= (blob[offset+1 +size - 1 - i] & 0xFF);
        }

        // If the number is negative (i.e., if the MSB is set), convert to negative long
        if ((blob[offset+size] & 0x80) != 0) { // Check the sign bit of the first byte
            value -= (1L << size*8); // Adjust value to be negative
        }

        return value;
    }

    private byte[] decodeBlob(byte[] blob, int offset) throws ABITException {
        // Blob is just an integer followed by a blob of the size of the integer
        int blobLength = (int)decodeInteger(blob, offset, 4);
        int blobOffset = offset+1+decodeMetadataLength(blob, offset);
        byte[] out = new byte[blobLength];
        System.arraycopy(blob, blobOffset, out, 0, blobLength);
        return out;
    }

    private String decodeString(byte[] blob, int offset) throws ABITException {
        // String is just a blob encoded with UTF-8
        int blobLength = (int)decodeInteger(blob, offset, 4);
        int blobOffset = offset+1+decodeMetadataLength(blob, offset);

        return new String(blob, blobOffset, blobLength, StandardCharsets.UTF_8);
    }

    private List<ABITObject> decodeArray(byte[] blob, int offset) throws ABITException {
        // Array is just an array inside a blob
        int blobLength = (int)decodeInteger(blob, offset, 4);
        int blobOffset = offset+1+decodeMetadataLength(blob, offset);

        List<ABITObject> out = new ArrayList<>();

        int idx = blobOffset;
        int blobEnd = blobOffset + blobLength;
        while (idx < blobEnd) {
            switch(decodeType(blob, idx)) {
                case 0:
                    decodeNull(blob, idx);
                    out.add(new ABITObject(NULL));
                    idx+= 1;
                    break;
                case 1:
                    out.add(new ABITObject(decodeBoolean(blob, idx)));
                    idx+= 1;
                    break;
                case 2:
                    out.add(new ABITObject(decodeInteger(blob, idx)));
                    idx+= 1 + decodeMetadataLength(blob, idx);
                    break;
                case 3:
                    out.add(new ABITObject(decodeBlob(blob, idx), true));
                    idx+= 1 + decodeMetadataLength(blob, idx) + decodeInteger(blob, idx, 4);
                    break;
                case 4:
                    out.add(new ABITObject(decodeString(blob, idx)));
                    idx+= 1 + decodeMetadataLength(blob, idx) + decodeInteger(blob, idx, 4);
                    break;
                case 5:
                    out.add(new ABITObject(decodeArray(blob, idx)));
                    idx+= 1 + decodeMetadataLength(blob, idx) + decodeInteger(blob, idx, 4);
                    break;
                case 6:
                    out.add(new ABITObject(decodeNestedTree(blob, idx)));
                    idx+= 1 + decodeMetadataLength(blob, idx) + decodeInteger(blob, idx, 4);
                    break;
            }
        }
        if(idx > blobEnd) {
            throw new ABITException("Corrupt ABIT");
        }
        return out;
    }

    private SortedMap<String, ABITObject> decodeNestedTree(byte[] blob, int offset) throws ABITException {
        // Nested tree is just a tree inside a blob
        int blobLength = (int)decodeInteger(blob, offset, 4);
        int blobOffset = offset+1+decodeMetadataLength(blob, offset);

        return decodeTree(blob, blobOffset, blobLength);
    }

    private SortedMap<String, ABITObject> decodeTree(byte[] blob, int offset, int length) throws ABITException {
        SortedMap<String, ABITObject> out = new TreeMap<>();

        int idx = offset;
        int blobEnd = offset + length;
        String lastKey = "";
        while (idx < blobEnd) {
            String key = decodeKey(blob, idx);

            if (idx != offset) {
                int cmp = keyCompare(key.getBytes(StandardCharsets.UTF_8), lastKey.getBytes(StandardCharsets.UTF_8));
                if (0 > cmp) {
                    throw new ABITException("Invalid key order or identical keys");
                }
            }
            lastKey = key;

            idx+= 1 + decodeKeyLength(blob, idx);

            switch(decodeType(blob, idx)) {
                case 0:
                    decodeNull(blob, idx);
                    out.put(key, new ABITObject(NULL));
                    idx+= 1;
                    break;
                case 1:
                    out.put(key, new ABITObject(decodeBoolean(blob, idx)));
                    idx+= 1;
                    break;
                case 2:
                    out.put(key, new ABITObject(decodeInteger(blob, idx)));
                    idx+= 1 + decodeMetadataLength(blob, idx);
                    break;
                case 3:
                    out.put(key, new ABITObject(decodeBlob(blob, idx), true));
                    idx+= 1 + decodeMetadataLength(blob, idx) + decodeInteger(blob, idx, 4);
                    break;
                case 4:
                    out.put(key, new ABITObject(decodeString(blob, idx)));
                    idx+= 1 + decodeMetadataLength(blob, idx) + decodeInteger(blob, idx, 4);
                    break;
                case 5:
                    out.put(key, new ABITObject(decodeArray(blob, idx)));
                    idx+= 1 + decodeMetadataLength(blob, idx) + decodeInteger(blob, idx, 4);
                    break;
                case 6:
                    out.put(key, new ABITObject(decodeNestedTree(blob, idx)));
                    idx+= 1 + decodeMetadataLength(blob, idx) + decodeInteger(blob, idx, 4);
                    break;
            }
        }
        if(idx > blobEnd) {
            throw new ABITException("Corrupt ABIT");
        }
        return out;
    }

    private byte[] encodeKey(String key) {
        byte[] keyRaw = key.getBytes(StandardCharsets.UTF_8);
        byte[] out = new byte[1 + keyRaw.length];
        out[0] = (byte) (keyRaw.length-1);
        System.arraycopy(keyRaw, 0, out, 1, keyRaw.length);
        return out;
    }

    private byte[] encodeNull() {
        byte[] out = new byte[1];
        out[0] = 0;
        return out;
    }

    private byte[] encodeBoolean(boolean bool) {
        if (bool) return new byte[] {0b00010001};
        return new byte[] {0b00000001};
    }

    private byte[] encodeInteger(long integer, int type) {
        // Calculate the number of bytes needed
        int metaDataLength = 8; // maximum is 8 bytes for a long
        if (integer != 0) {
            while (metaDataLength > 1) {
                long byteToCheck = (integer >> ((metaDataLength - 1) * 8)) & 0xFF;
                long nextSignificantByte = (integer >> ((metaDataLength - 2) * 8)) & 0xFF;
                
                // Check if the byte to check is either 0x00 (positive values) or 0xFF (negative values),
                // AND the next significant byte has the same sign, indicating it can be dropped.
                if ((byteToCheck == 0 && (nextSignificantByte & 0x80) == 0) || 
                    (byteToCheck == 0xFF && (nextSignificantByte & 0x80) == 0x80)) {
                    metaDataLength--;
                } else {
                    break;
                }
            }
        } else {
            metaDataLength = 1;
        }

        // Create the byte array of the appropriate size
        byte[] out = new byte[metaDataLength+1];
        for (int i = 0; i < metaDataLength; i++) {
            out[1+i] = (byte) ((integer >> (i * 8))&0xff);
        }
        
        out[0] = (byte) ((type & 0x0f) | ((metaDataLength-1) << 4));

        return out;
    }

    private byte[] encodeInteger(long integer) {
        return encodeInteger(integer, 2);
    }

    private byte[] encodeBlob(byte[] blob, int type) {
        byte[] blobLength = encodeInteger(blob.length, type);
        byte[] out = new byte[blobLength.length + blob.length];
        System.arraycopy(blobLength, 0, out, 0, blobLength.length);
        System.arraycopy(blob, 0, out, blobLength.length, blob.length);
        return out;
    }

    private byte[] encodeBlob(byte[] blob) {
        return encodeBlob(blob, 3);
    }

    private byte[] encodeString(String str) {
        return encodeBlob(str.getBytes(StandardCharsets.UTF_8), 4);
    }

    private byte[] encodeArray(List<ABITObject> array) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        for(int i = 0; i < array.size(); i++) {
            ABITObject obj = array.get(i);
            switch (obj.type) {
                case 0:
                    os.write(encodeNull());
                    break;
                case 1:
                    os.write(encodeBoolean(obj.booleanValue));
                    break;
                case 2:
                    os.write(encodeInteger(obj.intValue));
                    break;
                case 3:
                    os.write(encodeBlob(obj.blob));
                    break;
                case 4:
                    os.write(encodeString(obj.string));
                    break;
                case 5:
                    os.write(encodeArray(obj.array));
                    break;
                case 6:
                    os.write(encodeTree(obj.tree, true));
                    break;
            }
        }

        byte[] blob = os.toByteArray();
        byte[] arrayType = encodeInteger(blob.length, 5);
        byte[] out = new byte[arrayType.length + blob.length];
        System.arraycopy(arrayType, 0, out, 0, arrayType.length);
        System.arraycopy(blob, 0, out, arrayType.length, blob.length);
        return out;
    }

    private byte[] encodeTree(SortedMap<String, ABITObject> tree, boolean nested) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        
        Collection<String> keySet = tree.keySet();
        //List<String> keys = new ArrayList<>();
        List<byte[]> keys = new ArrayList<>();
        for (String str: keySet) {
            keys.add(str.getBytes(StandardCharsets.UTF_8));
        }

        // Sort using a custom comparator
        keys.sort(new KeysComparator());

        for(int i = 0; i < keys.size(); i++) {
            String key = new String(keys.get(i), StandardCharsets.UTF_8);
            ABITObject obj = tree.get(key);
            
            os.write(encodeKey(key));
            switch (obj.type) {
                case 0:
                    os.write(encodeNull());
                    break;
                case 1:
                    os.write(encodeBoolean(obj.booleanValue));
                    break;
                case 2:
                    os.write(encodeInteger(obj.intValue));
                    break;
                case 3:
                    os.write(encodeBlob(obj.blob));
                    break;
                case 4:
                    os.write(encodeString(obj.string));
                    break;
                case 5:
                    os.write(encodeArray(obj.array));
                    break;
                case 6:
                    os.write(encodeTree(obj.tree, true));
                    break;
            }
        }

        if (nested) {
            byte[] blob = os.toByteArray();
            byte[] treeType = encodeInteger(blob.length, 6);
            byte[] out = new byte[treeType.length + blob.length];
            System.arraycopy(treeType, 0, out, 0, treeType.length);
            System.arraycopy(blob, 0, out, treeType.length, blob.length);
            return out;
        }
        else {
            return os.toByteArray();
        }
    }

    private static int keyCompare(byte[] a, byte[] b) {
        // Step 1: Compare by length
        if (a.length != b.length) {
            return Integer.compare(a.length, b.length);
        }

        // Step 2: If lengths are the same, compare lexicographically
        for (int i = 0; i < a.length; i++) {
            int cmp = Byte.compare(a[i], b[i]);
            if (cmp != 0) {
                return cmp;
            }
        }

        // Step 3: Arrays are identical
        return 0;
    }

    // Custom comparator for sorting keys
    private static class KeysComparator implements Comparator<byte[]> {
        @Override
        public int compare(byte[] a, byte[] b) {
            return keyCompare(a, b);
        }
    }

    /**
     * Convert this ABITObject to its binary form.
     * @return byte array containing the abit object.
     * @throws IOException
     */
    public byte[] toByteArray() throws IOException {
        return encodeTree(this.tree, false);
    }

    /**
     * Associates the specified object with the specified key in this map. If the map previously contained a mapping for the key, the old value is replaced by the specified value.
     * @param key key with which the specified value is to be associated
     * @param object object to be associated with the specified key
     * @throws ABITException if the object is incompatible
     * @throws IllegalArgumentException if the key is incompatible
     */
    public void put(String key, NULL_t object) throws IllegalArgumentException {
        isCompatibleKey(key);
        isCompatibleNull(object);
        this.tree.put(key, new ABITObject(NULL));
    }

    /**
     * Associates the specified object with the specified key in this map. If the map previously contained a mapping for the key, the old value is replaced by the specified value.
     * @param key key with which the specified value is to be associated
     * @param object object to be associated with the specified key
     * @throws ABITException if the object is incompatible
     * @throws IllegalArgumentException if the key is incompatible
     */
    public void put(String key, boolean object) throws IllegalArgumentException  {
        isCompatibleKey(key);
        isCompatibleBoolean(object);
        this.tree.put(key, new ABITObject(object));
    }

    /**
     * Associates the specified object with the specified key in this map. If the map previously contained a mapping for the key, the old value is replaced by the specified value.
     * @param key key with which the specified value is to be associated
     * @param object object to be associated with the specified key
     * @throws ABITException if the object is incompatible
     * @throws IllegalArgumentException if the key is incompatible
     */
    public void put(String key, long object) throws IllegalArgumentException  {
        isCompatibleKey(key);
        isCompatibleInteger(object);
        this.tree.put(key, new ABITObject(object));
    }

    /**
     * Associates the specified object with the specified key in this map. If the map previously contained a mapping for the key, the old value is replaced by the specified value.
     * @param key key with which the specified value is to be associated
     * @param object object to be associated with the specified key
     * @throws ABITException if the object is incompatible
     * @throws IllegalArgumentException if the key is incompatible
     */
    public void put(String key, byte[] object) throws IllegalArgumentException  {
        isCompatibleKey(key);
        isCompatibleBlob(object);
        this.tree.put(key, new ABITObject(object, true));
    }

    /**
     * Associates the specified object with the specified key in this map. If the map previously contained a mapping for the key, the old value is replaced by the specified value.
     * @param key key with which the specified value is to be associated
     * @param object object to be associated with the specified key
     * @throws ABITException if the object is incompatible
     * @throws IllegalArgumentException if the key is incompatible
     */
    public void put(String key, String object) throws ABITException, IllegalArgumentException  {
        isCompatibleKey(key);
        isCompatibleString(object);
        this.tree.put(key, new ABITObject(object));
    }

    /**
     * Associates the specified object with the specified key in this map. If the map previously contained a mapping for the key, the old value is replaced by the specified value.
     * @param key key with which the specified value is to be associated
     * @param object object to be associated with the specified key
     * @throws IllegalArgumentException if the key is incompatible
     */
    public void put(String key, ABITArray object) throws IllegalArgumentException  {
        isCompatibleKey(key);
        this.tree.put(key, new ABITObject(object.array));
    }

    /**
     * Associates the specified object with the specified key in this map. If the map previously contained a mapping for the key, the old value is replaced by the specified value.
     * @param key key with which the specified value is to be associated
     * @param object object to be associated with the specified key
     * @throws IllegalArgumentException if the key is incompatible
     */
    public void put(String key, ABITObject object) throws IllegalArgumentException  {
        isCompatibleKey(key);
        this.tree.put(key, object);
    }

    @Override
    public String toString() {
        try {
            String out = "";
            for (byte b : this.toByteArray()) {
                out+= String.format("%02X ", b);
            }
            return out;
        }
        catch (IOException e) {
            return "ERROR";
        }
    }

    Object getSelf() throws ABITException {
        switch(this.type) {
            case 0:
                return NULL;
            case 1:
                return this.booleanValue;
            case 2:
                return this.intValue;
            case 3:
                return this.blob;
            case 4:
                return this.string;
            case 5:
                return new ABITArray(this.array);
            case 6:
                return this;
            default:
                throw new ABITException("Invalid ABITObject");
        }
    }

    static void isCompatibleNull(NULL_t Null) {
        // obviously supported...
        return;
        // throw new ABITException("Incompatible null, this should literally not happen");
    }

    static void isCompatibleInteger(long integer) {
        // a signed long will always be compatible as it's the exact same spec.
        return;
        // throw new ABITException("Incompatible integer, this should literally not happen");
    }

    static void isCompatibleBoolean(boolean bool) {
        // obviously all values of a boolean is compatible...
        return;
        // throw new ABITException("Incompatible integer, this should literally not happen");
    }

    static void isCompatibleString(String str) throws ABITException {
        // string supports up to 4 bytes of int describing length (2s compliment signed)
        // due to supporting all UTF-8 characters, it's not guaranteed that a string when encoded is short enough to fit.
        long totalLength = 0;
        long charLeft = str.length();
        final long maxSegment = Integer.MAX_VALUE / 5; // as UTF-8 expands up to 4 bytes per character, so we divide by 5 just to be sure
        while(charLeft > 0) {
            long idx = str.length()-charLeft;
            long idx_end = idx+maxSegment;
            if ((long) str.length() < idx_end) {
                idx_end = (long) str.length();
            }
            totalLength+= str.substring((int)idx, (int)idx_end).getBytes(StandardCharsets.UTF_8).length;
            charLeft-= idx_end-idx;
        }
        if (totalLength > Integer.MAX_VALUE) {
            throw new ABITException("Incompatible string, it's too long");
        }
    }

    static void isCompatibleBlob(byte[] bytes) {
        // if a blob fits inside java, it is compatible
        return;
        // throw new ABITException("Incompatible blob, this should literally not happen");
    }

    static void isCompatibleKey(String key) throws IllegalArgumentException {
        long keyLength = key.getBytes(StandardCharsets.UTF_8).length;
        if (keyLength > 256 || keyLength < 1) {
            throw new IllegalArgumentException("Incompatible key, must be between 1 - 256 bytes when encoded with UTF-8");
        }
    }

    private JSONArray getJsonArray(int base58CutOff) {
        JSONArray out = new JSONArray();

        for(int i = 0; i < this.array.size(); i++) {
            ABITObject obj = this.array.get(i);
            
            switch (obj.type) {
                case 0:
                    out.put(JSONObject.NULL);
                    break;
                case 1:
                    out.put(obj.booleanValue);
                    break;
                case 2:
                    out.put(obj.intValue);
                    break;
                case 3:
                    if (base58CutOff < obj.blob.length) {
                        out.put(Multibase.encode(Multibase.Base.Base64Url, obj.blob));
                    }
                    else {
                        out.put(Multibase.encode(Multibase.Base.Base58BTC, obj.blob));
                    }
                    break;
                case 4:
                    out.put(obj.string);
                    break;
                case 5:
                    out.put(obj.getJsonArray(base58CutOff));
                    break;
                case 6:
                    out.put(obj.getJson(base58CutOff));
                    break;
            }
        }

        return out;
    }

    public JSONObject getJson(int base58CutOff) {
        JSONObject out = new JSONObject();
        
        Collection<String> keySet = this.tree.keySet();
        //List<String> keys = new ArrayList<>();
        List<byte[]> keys = new ArrayList<>();
        for (String str: keySet) {
            keys.add(str.getBytes(StandardCharsets.UTF_8));
        }

        // Sort using a custom comparator
        keys.sort(new KeysComparator());

        for(int i = 0; i < keys.size(); i++) {
            String key = new String(keys.get(i), StandardCharsets.UTF_8);
            ABITObject obj = tree.get(key);
            
            switch (obj.type) {
                case 0:
                    out.put(key, JSONObject.NULL);
                    break;
                case 1:
                    out.put(key, obj.booleanValue);
                    break;
                case 2:
                    out.put(key, obj.intValue);
                    break;
                case 3:
                    if (base58CutOff < obj.blob.length) {
                        out.put(key, Multibase.encode(Multibase.Base.Base64Url, obj.blob));
                    }
                    else {
                        out.put(key, Multibase.encode(Multibase.Base.Base58BTC, obj.blob));
                    }
                    break;
                case 4:
                    out.put(key, obj.string);
                    break;
                case 5:
                    out.put(key, obj.getJsonArray(base58CutOff));
                    break;
                case 6:
                    out.put(key, obj.getJson(base58CutOff));
                    break;
            }
        }

        return out;
    }

    public JSONObject getJson() {
        return getJson(32);
    }
}
