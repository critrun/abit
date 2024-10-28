package org.deepslate.abit;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import io.ipfs.multibase.Multibase;
import java.util.Iterator;

import java.util.ArrayList;

public class ABITArray {
    List<ABITObject> array = new ArrayList<>();

    ABITArray (ABITArray array) {
        this.array.clear();
        this.array = array.array;
    }

    ABITArray (List<ABITObject> array) {
        this.array.clear();
        this.array = array;
    }

    /**
     * Initialize an empty ABITArray.
     */
    public ABITArray () {
        this.array.clear();
    }

    ABITArray(JSONArray json, String binaryRegex, String selfKey) throws ABITException, IllegalStateException {
        this.array.clear();

        boolean arrayHoldsBinary = selfKey.matches(binaryRegex);

        for (Iterator<Object> iterator = json.iterator(); iterator.hasNext(); ) {
            Object obj = iterator.next();
            switch (obj) {
                case Boolean b -> {
                    this.add(b);
                }
                case Integer i -> {
                    this.add(i);
                }
                case String s -> {
                    if (arrayHoldsBinary) {
                        this.add(Multibase.decode(s));
                    }
                    else {
                        this.add(s);
                    }
                }
                case JSONArray a -> {
                    this.add(new ABITArray(a, binaryRegex, selfKey));
                }
                case JSONObject o -> {
                    this.add(new ABITObject(o, binaryRegex));
                }
                default -> {
                    if (JSONObject.NULL.equals(obj)) {
                        this.add(ABITObject.NULL);
                    }
                    else {
                        throw new ABITException("Unsupported object type in json document");
                    }
                }
            }
        }
    }

    /**
     * Removes all of the elements from this list (optional operation). The list will be empty after this call returns.
     */
    public void clear() {
        this.array.clear();
    }

    /**
     * Get the type of the object at specified index.
     * @param index index of the object to check the type of
     * @return string of type: "null" / "boolean" / "integer" / "blob" / "string" / "array" / "tree"
     * @throws ABITException
     */
    public String getType(int index) throws ABITException {

        int obj = this.array.get(index).type;
        switch (obj) {
            case 0:
                return "null";
            case 1:
                return "boolean";
            case 2:
                return "integer";
            case 3:
                return "blob";
            case 4:
                return "string";
            case 5: 
                return "array";
            case 6:
                return "tree";
            default:
                throw new ABITException("Invalid type");
        }
    }

    /**
     * Get object at index from array
     * @param index
     * @return null object
     * @throws ABITException
     */
    public ABITObject.NULL_t getNull(int index) throws ABITException {
        if(this.array.get(index).type == 0) {
            return ABITObject.NULL;
        }
        else {
            throw new ABITException("Object is not of type null");
        }
    }

    /**
     * Get object at index from array
     * @param index
     * @return boolean
     * @throws ABITException
     */
    public boolean getBoolean(int index) throws ABITException {
        if(this.array.get(index).type == 1) {
            return this.array.get(index).booleanValue;
        }
        else {
            throw new ABITException("Object is not of type boolean");
        }
    }

    /**
     * Get object at index from array
     * @param index
     * @return integer
     * @throws ABITException
     */
    public long getInteger(int index) throws ABITException {
        if(this.array.get(index).type == 2) {
            return this.array.get(index).intValue;
        }
        else {
            throw new ABITException("Object is not of type boolean");
        }
    }

    /**
     * Get object at index from array
     * @param index
     * @return blob
     * @throws ABITException
     */
    public byte[] getBlob(int index) throws ABITException {
        if(this.array.get(index).type == 3) {
            return this.array.get(index).blob;
        }
        else {
            throw new ABITException("Object is not of type boolean");
        }
    }

    /**
     * Get object at index from array
     * @param index
     * @return string
     * @throws ABITException
     */
    public String getString(int index) throws ABITException {
        if(this.array.get(index).type == 4) {
            return this.array.get(index).string;
        }
        else {
            throw new ABITException("Object is not of type boolean");
        }
    }

    /**
     * Get object at index from array
     * @param index
     * @return ABITArray
     * @throws ABITException
     */
    public ABITArray getArray(int index) throws ABITException {
        if(this.array.get(index).type == 5) {
            return new ABITArray(this.array.get(index).array);
        }
        else {
            throw new ABITException("Object is not of type boolean");
        }
    }

    /**
     * Get object at index from array
     * @param index
     * @return ABITObject
     * @throws ABITException
     */
    public ABITObject getTree(int index) throws ABITException {
        if(this.array.get(index).type == 6) {
            return this.array.get(index);
        }
        else {
            throw new ABITException("Object is not of type boolean");
        }
    }

    public boolean isEmpty() {
        return this.array.isEmpty();
    }

    public ABITObject remove(int index) {
        return this.array.remove(index);
    }

    /**
     * Inserts the specified element at the specified position in this list (optional operation). Shifts the element currently at that position (if any) and any subsequent elements to the right (adds one to their indices).
     * @param index index at which the specified element is to be inserted
     * @param element element to be inserted
     */
    public void add(int index, ABITObject.NULL_t element) {
        this.array.add(index, new ABITObject(ABITObject.NULL));
    }
    
    /**
     * Inserts the specified element at the specified position in this list (optional operation). Shifts the element currently at that position (if any) and any subsequent elements to the right (adds one to their indices).
     * @param index index at which the specified element is to be inserted
     * @param element element to be inserted
     */
    public void add(int index, boolean element) {
        this.array.add(index, new ABITObject(element));
    }

    /**
     * Inserts the specified element at the specified position in this list (optional operation). Shifts the element currently at that position (if any) and any subsequent elements to the right (adds one to their indices).
     * @param index index at which the specified element is to be inserted
     * @param element element to be inserted
     */
    public void add(int index, long element) {
        this.array.add(index, new ABITObject(element));
    }

    /**
     * Inserts the specified element at the specified position in this list (optional operation). Shifts the element currently at that position (if any) and any subsequent elements to the right (adds one to their indices).
     * @param index index at which the specified element is to be inserted
     * @param element element to be inserted
     */
    public void add(int index, byte[] element) {
        this.array.add(index, new ABITObject(element, true));
    }

    /**
     * Inserts the specified element at the specified position in this list (optional operation). Shifts the element currently at that position (if any) and any subsequent elements to the right (adds one to their indices).
     * @param index index at which the specified element is to be inserted
     * @param element element to be inserted
     */
    public void add(int index, String element) {
        this.array.add(index, new ABITObject(element));
    }

    /**
     * Inserts the specified element at the specified position in this list (optional operation). Shifts the element currently at that position (if any) and any subsequent elements to the right (adds one to their indices).
     * @param index index at which the specified element is to be inserted
     * @param element element to be inserted
     */
    public void add(int index, ABITArray element) {
        this.array.add(index, new ABITObject(element.array));
    }

    /**
     * Inserts the specified element at the specified position in this list (optional operation). Shifts the element currently at that position (if any) and any subsequent elements to the right (adds one to their indices).
     * @param index index at which the specified element is to be inserted
     * @param element element to be inserted
     */
    public void add(int index, ABITObject element) {
        this.array.add(index, element);
    }

    /**
     * Appends the specified element to the end of this list.
     * @param element element to be appended to this list
     */
    public void add(ABITObject.NULL_t element) {
        this.array.add(new ABITObject(ABITObject.NULL));
    }

    /**
     * Appends the specified element to the end of this list.
     * @param element element to be appended to this list
     */
    public void add(boolean element) {
        this.array.add(new ABITObject(element));
    }

    /**
     * Appends the specified element to the end of this list.
     * @param element element to be appended to this list
     */
    public void add(long element) {
        this.array.add(new ABITObject(element));
    }

    /**
     * Appends the specified element to the end of this list.
     * @param element element to be appended to this list
     */
    public void add(byte[] element) {
        this.array.add(new ABITObject(element, true));
    }

    /**
     * Appends the specified element to the end of this list.
     * @param element element to be appended to this list
     */
    public void add(String element) {
        this.array.add(new ABITObject(element));
    }

    /**
     * Appends the specified element to the end of this list.
     * @param element element to be appended to this list
     */
    public void add(ABITArray element) {
        this.array.add(new ABITObject(element.array));
    }

    /**
     * Appends the specified element to the end of this list.
     * @param element element to be appended to this list
     */
    public void add(ABITObject element) {
        this.array.add(element);
    }

    /**
     * Returns the number of elements in this list.  If this list contains
     * more than {@code Integer.MAX_VALUE} elements, returns
     * {@code Integer.MAX_VALUE}.
     *
     * @return the number of elements in this list
     */
    public int size() {
        return this.array.size();
    }
}
