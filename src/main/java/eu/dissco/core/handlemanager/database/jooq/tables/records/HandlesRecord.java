/*
 * This file is generated by jOOQ.
 */
package eu.dissco.core.handlemanager.database.jooq.tables.records;


import eu.dissco.core.handlemanager.database.jooq.tables.Handles;
import org.jooq.Field;
import org.jooq.Record12;
import org.jooq.Record2;
import org.jooq.Row12;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class HandlesRecord extends UpdatableRecordImpl<HandlesRecord> implements
    Record12<byte[], Integer, byte[], byte[], Short, Integer, Long, String, Boolean, Boolean, Boolean, Boolean> {

  private static final long serialVersionUID = 1L;

  /**
   * Setter for <code>public.handles.handle</code>.
   */
  public void setHandle(byte[] value) {
    set(0, value);
  }

  /**
   * Getter for <code>public.handles.handle</code>.
   */
  public byte[] getHandle() {
    return (byte[]) get(0);
  }

  /**
   * Setter for <code>public.handles.idx</code>.
   */
  public void setIdx(Integer value) {
    set(1, value);
  }

  /**
   * Getter for <code>public.handles.idx</code>.
   */
  public Integer getIdx() {
    return (Integer) get(1);
  }

  /**
   * Setter for <code>public.handles.type</code>.
   */
  public void setType(byte[] value) {
    set(2, value);
  }

  /**
   * Getter for <code>public.handles.type</code>.
   */
  public byte[] getType() {
    return (byte[]) get(2);
  }

  /**
   * Setter for <code>public.handles.data</code>.
   */
  public void setData(byte[] value) {
    set(3, value);
  }

  /**
   * Getter for <code>public.handles.data</code>.
   */
  public byte[] getData() {
    return (byte[]) get(3);
  }

  /**
   * Setter for <code>public.handles.ttl_type</code>.
   */
  public void setTtlType(Short value) {
    set(4, value);
  }

  /**
   * Getter for <code>public.handles.ttl_type</code>.
   */
  public Short getTtlType() {
    return (Short) get(4);
  }

  /**
   * Setter for <code>public.handles.ttl</code>.
   */
  public void setTtl(Integer value) {
    set(5, value);
  }

  /**
   * Getter for <code>public.handles.ttl</code>.
   */
  public Integer getTtl() {
    return (Integer) get(5);
  }

  /**
   * Setter for <code>public.handles.timestamp</code>.
   */
  public void setTimestamp(Long value) {
    set(6, value);
  }

  /**
   * Getter for <code>public.handles.timestamp</code>.
   */
  public Long getTimestamp() {
    return (Long) get(6);
  }

  /**
   * Setter for <code>public.handles.refs</code>.
   */
  public void setRefs(String value) {
    set(7, value);
  }

  /**
   * Getter for <code>public.handles.refs</code>.
   */
  public String getRefs() {
    return (String) get(7);
  }

  /**
   * Setter for <code>public.handles.admin_read</code>.
   */
  public void setAdminRead(Boolean value) {
    set(8, value);
  }

  /**
   * Getter for <code>public.handles.admin_read</code>.
   */
  public Boolean getAdminRead() {
    return (Boolean) get(8);
  }

  /**
   * Setter for <code>public.handles.admin_write</code>.
   */
  public void setAdminWrite(Boolean value) {
    set(9, value);
  }

  /**
   * Getter for <code>public.handles.admin_write</code>.
   */
  public Boolean getAdminWrite() {
    return (Boolean) get(9);
  }

  /**
   * Setter for <code>public.handles.pub_read</code>.
   */
  public void setPubRead(Boolean value) {
    set(10, value);
  }

  /**
   * Getter for <code>public.handles.pub_read</code>.
   */
  public Boolean getPubRead() {
    return (Boolean) get(10);
  }

  /**
   * Setter for <code>public.handles.pub_write</code>.
   */
  public void setPubWrite(Boolean value) {
    set(11, value);
  }

  /**
   * Getter for <code>public.handles.pub_write</code>.
   */
  public Boolean getPubWrite() {
    return (Boolean) get(11);
  }

  // -------------------------------------------------------------------------
  // Primary key information
  // -------------------------------------------------------------------------

  @Override
  public Record2<byte[], Integer> key() {
    return (Record2) super.key();
  }

  // -------------------------------------------------------------------------
  // Record12 type implementation
  // -------------------------------------------------------------------------

  @Override
  public Row12<byte[], Integer, byte[], byte[], Short, Integer, Long, String, Boolean, Boolean, Boolean, Boolean> fieldsRow() {
    return (Row12) super.fieldsRow();
  }

  @Override
  public Row12<byte[], Integer, byte[], byte[], Short, Integer, Long, String, Boolean, Boolean, Boolean, Boolean> valuesRow() {
    return (Row12) super.valuesRow();
  }

  @Override
  public Field<byte[]> field1() {
    return Handles.HANDLES.HANDLE;
  }

  @Override
  public Field<Integer> field2() {
    return Handles.HANDLES.IDX;
  }

  @Override
  public Field<byte[]> field3() {
    return Handles.HANDLES.TYPE;
  }

  @Override
  public Field<byte[]> field4() {
    return Handles.HANDLES.DATA;
  }

  @Override
  public Field<Short> field5() {
    return Handles.HANDLES.TTL_TYPE;
  }

  @Override
  public Field<Integer> field6() {
    return Handles.HANDLES.TTL;
  }

  @Override
  public Field<Long> field7() {
    return Handles.HANDLES.TIMESTAMP;
  }

  @Override
  public Field<String> field8() {
    return Handles.HANDLES.REFS;
  }

  @Override
  public Field<Boolean> field9() {
    return Handles.HANDLES.ADMIN_READ;
  }

  @Override
  public Field<Boolean> field10() {
    return Handles.HANDLES.ADMIN_WRITE;
  }

  @Override
  public Field<Boolean> field11() {
    return Handles.HANDLES.PUB_READ;
  }

  @Override
  public Field<Boolean> field12() {
    return Handles.HANDLES.PUB_WRITE;
  }

  @Override
  public byte[] component1() {
    return getHandle();
  }

  @Override
  public Integer component2() {
    return getIdx();
  }

  @Override
  public byte[] component3() {
    return getType();
  }

  @Override
  public byte[] component4() {
    return getData();
  }

  @Override
  public Short component5() {
    return getTtlType();
  }

  @Override
  public Integer component6() {
    return getTtl();
  }

  @Override
  public Long component7() {
    return getTimestamp();
  }

  @Override
  public String component8() {
    return getRefs();
  }

  @Override
  public Boolean component9() {
    return getAdminRead();
  }

  @Override
  public Boolean component10() {
    return getAdminWrite();
  }

  @Override
  public Boolean component11() {
    return getPubRead();
  }

  @Override
  public Boolean component12() {
    return getPubWrite();
  }

  @Override
  public byte[] value1() {
    return getHandle();
  }

  @Override
  public Integer value2() {
    return getIdx();
  }

  @Override
  public byte[] value3() {
    return getType();
  }

  @Override
  public byte[] value4() {
    return getData();
  }

  @Override
  public Short value5() {
    return getTtlType();
  }

  @Override
  public Integer value6() {
    return getTtl();
  }

  @Override
  public Long value7() {
    return getTimestamp();
  }

  @Override
  public String value8() {
    return getRefs();
  }

  @Override
  public Boolean value9() {
    return getAdminRead();
  }

  @Override
  public Boolean value10() {
    return getAdminWrite();
  }

  @Override
  public Boolean value11() {
    return getPubRead();
  }

  @Override
  public Boolean value12() {
    return getPubWrite();
  }

  @Override
  public HandlesRecord value1(byte[] value) {
    setHandle(value);
    return this;
  }

  @Override
  public HandlesRecord value2(Integer value) {
    setIdx(value);
    return this;
  }

  @Override
  public HandlesRecord value3(byte[] value) {
    setType(value);
    return this;
  }

  @Override
  public HandlesRecord value4(byte[] value) {
    setData(value);
    return this;
  }

  @Override
  public HandlesRecord value5(Short value) {
    setTtlType(value);
    return this;
  }

  @Override
  public HandlesRecord value6(Integer value) {
    setTtl(value);
    return this;
  }

  @Override
  public HandlesRecord value7(Long value) {
    setTimestamp(value);
    return this;
  }

  @Override
  public HandlesRecord value8(String value) {
    setRefs(value);
    return this;
  }

  @Override
  public HandlesRecord value9(Boolean value) {
    setAdminRead(value);
    return this;
  }

  @Override
  public HandlesRecord value10(Boolean value) {
    setAdminWrite(value);
    return this;
  }

  @Override
  public HandlesRecord value11(Boolean value) {
    setPubRead(value);
    return this;
  }

  @Override
  public HandlesRecord value12(Boolean value) {
    setPubWrite(value);
    return this;
  }

  @Override
  public HandlesRecord values(byte[] value1, Integer value2, byte[] value3, byte[] value4,
      Short value5, Integer value6, Long value7, String value8, Boolean value9, Boolean value10,
      Boolean value11, Boolean value12) {
    value1(value1);
    value2(value2);
    value3(value3);
    value4(value4);
    value5(value5);
    value6(value6);
    value7(value7);
    value8(value8);
    value9(value9);
    value10(value10);
    value11(value11);
    value12(value12);
    return this;
  }

  // -------------------------------------------------------------------------
  // Constructors
  // -------------------------------------------------------------------------

  /**
   * Create a detached HandlesRecord
   */
  public HandlesRecord() {
    super(Handles.HANDLES);
  }

  /**
   * Create a detached, initialised HandlesRecord
   */
  public HandlesRecord(byte[] handle, Integer idx, byte[] type, byte[] data, Short ttlType,
      Integer ttl, Long timestamp, String refs, Boolean adminRead, Boolean adminWrite,
      Boolean pubRead, Boolean pubWrite) {
    super(Handles.HANDLES);

    setHandle(handle);
    setIdx(idx);
    setType(type);
    setData(data);
    setTtlType(ttlType);
    setTtl(ttl);
    setTimestamp(timestamp);
    setRefs(refs);
    setAdminRead(adminRead);
    setAdminWrite(adminWrite);
    setPubRead(pubRead);
    setPubWrite(pubWrite);
  }
}
