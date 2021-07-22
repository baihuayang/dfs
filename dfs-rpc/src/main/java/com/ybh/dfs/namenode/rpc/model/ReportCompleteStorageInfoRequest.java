// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: NameNodeRpcModel.proto

package com.ybh.dfs.namenode.rpc.model;

/**
 * Protobuf type {@code com.ybh.dfs.namenode.rpc.ReportCompleteStorageInfoRequest}
 */
public  final class ReportCompleteStorageInfoRequest extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:com.ybh.dfs.namenode.rpc.ReportCompleteStorageInfoRequest)
    ReportCompleteStorageInfoRequestOrBuilder {
  // Use ReportCompleteStorageInfoRequest.newBuilder() to construct.
  private ReportCompleteStorageInfoRequest(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private ReportCompleteStorageInfoRequest() {
    ip_ = "";
    hostname_ = "";
    filenames_ = "";
    storedDataSize_ = 0L;
  }

  @Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return com.google.protobuf.UnknownFieldSet.getDefaultInstance();
  }
  private ReportCompleteStorageInfoRequest(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    this();
    int mutable_bitField0_ = 0;
    try {
      boolean done = false;
      while (!done) {
        int tag = input.readTag();
        switch (tag) {
          case 0:
            done = true;
            break;
          default: {
            if (!input.skipField(tag)) {
              done = true;
            }
            break;
          }
          case 10: {
            String s = input.readStringRequireUtf8();

            ip_ = s;
            break;
          }
          case 18: {
            String s = input.readStringRequireUtf8();

            hostname_ = s;
            break;
          }
          case 26: {
            String s = input.readStringRequireUtf8();

            filenames_ = s;
            break;
          }
          case 32: {

            storedDataSize_ = input.readInt64();
            break;
          }
        }
      }
    } catch (com.google.protobuf.InvalidProtocolBufferException e) {
      throw e.setUnfinishedMessage(this);
    } catch (java.io.IOException e) {
      throw new com.google.protobuf.InvalidProtocolBufferException(
          e).setUnfinishedMessage(this);
    } finally {
      makeExtensionsImmutable();
    }
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return NameNodeRpcModel.internal_static_com_ybh_dfs_namenode_rpc_ReportCompleteStorageInfoRequest_descriptor;
  }

  protected FieldAccessorTable
      internalGetFieldAccessorTable() {
    return NameNodeRpcModel.internal_static_com_ybh_dfs_namenode_rpc_ReportCompleteStorageInfoRequest_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            ReportCompleteStorageInfoRequest.class, Builder.class);
  }

  public static final int IP_FIELD_NUMBER = 1;
  private volatile Object ip_;
  /**
   * <code>optional string ip = 1;</code>
   */
  public String getIp() {
    Object ref = ip_;
    if (ref instanceof String) {
      return (String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      String s = bs.toStringUtf8();
      ip_ = s;
      return s;
    }
  }
  /**
   * <code>optional string ip = 1;</code>
   */
  public com.google.protobuf.ByteString
      getIpBytes() {
    Object ref = ip_;
    if (ref instanceof String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (String) ref);
      ip_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int HOSTNAME_FIELD_NUMBER = 2;
  private volatile Object hostname_;
  /**
   * <code>optional string hostname = 2;</code>
   */
  public String getHostname() {
    Object ref = hostname_;
    if (ref instanceof String) {
      return (String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      String s = bs.toStringUtf8();
      hostname_ = s;
      return s;
    }
  }
  /**
   * <code>optional string hostname = 2;</code>
   */
  public com.google.protobuf.ByteString
      getHostnameBytes() {
    Object ref = hostname_;
    if (ref instanceof String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (String) ref);
      hostname_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int FILENAMES_FIELD_NUMBER = 3;
  private volatile Object filenames_;
  /**
   * <code>optional string filenames = 3;</code>
   */
  public String getFilenames() {
    Object ref = filenames_;
    if (ref instanceof String) {
      return (String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      String s = bs.toStringUtf8();
      filenames_ = s;
      return s;
    }
  }
  /**
   * <code>optional string filenames = 3;</code>
   */
  public com.google.protobuf.ByteString
      getFilenamesBytes() {
    Object ref = filenames_;
    if (ref instanceof String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (String) ref);
      filenames_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int STOREDDATASIZE_FIELD_NUMBER = 4;
  private long storedDataSize_;
  /**
   * <code>optional int64 storedDataSize = 4;</code>
   */
  public long getStoredDataSize() {
    return storedDataSize_;
  }

  private byte memoizedIsInitialized = -1;
  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized == 1) return true;
    if (isInitialized == 0) return false;

    memoizedIsInitialized = 1;
    return true;
  }

  public void writeTo(com.google.protobuf.CodedOutputStream output)
                      throws java.io.IOException {
    if (!getIpBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1, ip_);
    }
    if (!getHostnameBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 2, hostname_);
    }
    if (!getFilenamesBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 3, filenames_);
    }
    if (storedDataSize_ != 0L) {
      output.writeInt64(4, storedDataSize_);
    }
  }

  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (!getIpBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, ip_);
    }
    if (!getHostnameBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(2, hostname_);
    }
    if (!getFilenamesBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(3, filenames_);
    }
    if (storedDataSize_ != 0L) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt64Size(4, storedDataSize_);
    }
    memoizedSize = size;
    return size;
  }

  private static final long serialVersionUID = 0L;
  @Override
  public boolean equals(final Object obj) {
    if (obj == this) {
     return true;
    }
    if (!(obj instanceof ReportCompleteStorageInfoRequest)) {
      return super.equals(obj);
    }
    ReportCompleteStorageInfoRequest other = (ReportCompleteStorageInfoRequest) obj;

    boolean result = true;
    result = result && getIp()
        .equals(other.getIp());
    result = result && getHostname()
        .equals(other.getHostname());
    result = result && getFilenames()
        .equals(other.getFilenames());
    result = result && (getStoredDataSize()
        == other.getStoredDataSize());
    return result;
  }

  @Override
  public int hashCode() {
    if (memoizedHashCode != 0) {
      return memoizedHashCode;
    }
    int hash = 41;
    hash = (19 * hash) + getDescriptorForType().hashCode();
    hash = (37 * hash) + IP_FIELD_NUMBER;
    hash = (53 * hash) + getIp().hashCode();
    hash = (37 * hash) + HOSTNAME_FIELD_NUMBER;
    hash = (53 * hash) + getHostname().hashCode();
    hash = (37 * hash) + FILENAMES_FIELD_NUMBER;
    hash = (53 * hash) + getFilenames().hashCode();
    hash = (37 * hash) + STOREDDATASIZE_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
        getStoredDataSize());
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static ReportCompleteStorageInfoRequest parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static ReportCompleteStorageInfoRequest parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static ReportCompleteStorageInfoRequest parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static ReportCompleteStorageInfoRequest parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static ReportCompleteStorageInfoRequest parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static ReportCompleteStorageInfoRequest parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static ReportCompleteStorageInfoRequest parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static ReportCompleteStorageInfoRequest parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static ReportCompleteStorageInfoRequest parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static ReportCompleteStorageInfoRequest parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public Builder newBuilderForType() { return newBuilder(); }
  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }
  public static Builder newBuilder(ReportCompleteStorageInfoRequest prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }
  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE
        ? new Builder() : new Builder().mergeFrom(this);
  }

  @Override
  protected Builder newBuilderForType(
      BuilderParent parent) {
    Builder builder = new Builder(parent);
    return builder;
  }
  /**
   * Protobuf type {@code com.ybh.dfs.namenode.rpc.ReportCompleteStorageInfoRequest}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:com.ybh.dfs.namenode.rpc.ReportCompleteStorageInfoRequest)
      ReportCompleteStorageInfoRequestOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return NameNodeRpcModel.internal_static_com_ybh_dfs_namenode_rpc_ReportCompleteStorageInfoRequest_descriptor;
    }

    protected FieldAccessorTable
        internalGetFieldAccessorTable() {
      return NameNodeRpcModel.internal_static_com_ybh_dfs_namenode_rpc_ReportCompleteStorageInfoRequest_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              ReportCompleteStorageInfoRequest.class, Builder.class);
    }

    // Construct using com.ybh.dfs.namenode.rpc.model.ReportCompleteStorageInfoRequest.newBuilder()
    private Builder() {
      maybeForceBuilderInitialization();
    }

    private Builder(
        BuilderParent parent) {
      super(parent);
      maybeForceBuilderInitialization();
    }
    private void maybeForceBuilderInitialization() {
      if (com.google.protobuf.GeneratedMessageV3
              .alwaysUseFieldBuilders) {
      }
    }
    public Builder clear() {
      super.clear();
      ip_ = "";

      hostname_ = "";

      filenames_ = "";

      storedDataSize_ = 0L;

      return this;
    }

    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return NameNodeRpcModel.internal_static_com_ybh_dfs_namenode_rpc_ReportCompleteStorageInfoRequest_descriptor;
    }

    public ReportCompleteStorageInfoRequest getDefaultInstanceForType() {
      return ReportCompleteStorageInfoRequest.getDefaultInstance();
    }

    public ReportCompleteStorageInfoRequest build() {
      ReportCompleteStorageInfoRequest result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    public ReportCompleteStorageInfoRequest buildPartial() {
      ReportCompleteStorageInfoRequest result = new ReportCompleteStorageInfoRequest(this);
      result.ip_ = ip_;
      result.hostname_ = hostname_;
      result.filenames_ = filenames_;
      result.storedDataSize_ = storedDataSize_;
      onBuilt();
      return result;
    }

    public Builder clone() {
      return (Builder) super.clone();
    }
    public Builder setField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        Object value) {
      return (Builder) super.setField(field, value);
    }
    public Builder clearField(
        com.google.protobuf.Descriptors.FieldDescriptor field) {
      return (Builder) super.clearField(field);
    }
    public Builder clearOneof(
        com.google.protobuf.Descriptors.OneofDescriptor oneof) {
      return (Builder) super.clearOneof(oneof);
    }
    public Builder setRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        int index, Object value) {
      return (Builder) super.setRepeatedField(field, index, value);
    }
    public Builder addRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        Object value) {
      return (Builder) super.addRepeatedField(field, value);
    }
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof ReportCompleteStorageInfoRequest) {
        return mergeFrom((ReportCompleteStorageInfoRequest)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(ReportCompleteStorageInfoRequest other) {
      if (other == ReportCompleteStorageInfoRequest.getDefaultInstance()) return this;
      if (!other.getIp().isEmpty()) {
        ip_ = other.ip_;
        onChanged();
      }
      if (!other.getHostname().isEmpty()) {
        hostname_ = other.hostname_;
        onChanged();
      }
      if (!other.getFilenames().isEmpty()) {
        filenames_ = other.filenames_;
        onChanged();
      }
      if (other.getStoredDataSize() != 0L) {
        setStoredDataSize(other.getStoredDataSize());
      }
      onChanged();
      return this;
    }

    public final boolean isInitialized() {
      return true;
    }

    public Builder mergeFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      ReportCompleteStorageInfoRequest parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (ReportCompleteStorageInfoRequest) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }

    private Object ip_ = "";
    /**
     * <code>optional string ip = 1;</code>
     */
    public String getIp() {
      Object ref = ip_;
      if (!(ref instanceof String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        String s = bs.toStringUtf8();
        ip_ = s;
        return s;
      } else {
        return (String) ref;
      }
    }
    /**
     * <code>optional string ip = 1;</code>
     */
    public com.google.protobuf.ByteString
        getIpBytes() {
      Object ref = ip_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (String) ref);
        ip_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>optional string ip = 1;</code>
     */
    public Builder setIp(
        String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      ip_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>optional string ip = 1;</code>
     */
    public Builder clearIp() {
      
      ip_ = getDefaultInstance().getIp();
      onChanged();
      return this;
    }
    /**
     * <code>optional string ip = 1;</code>
     */
    public Builder setIpBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      ip_ = value;
      onChanged();
      return this;
    }

    private Object hostname_ = "";
    /**
     * <code>optional string hostname = 2;</code>
     */
    public String getHostname() {
      Object ref = hostname_;
      if (!(ref instanceof String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        String s = bs.toStringUtf8();
        hostname_ = s;
        return s;
      } else {
        return (String) ref;
      }
    }
    /**
     * <code>optional string hostname = 2;</code>
     */
    public com.google.protobuf.ByteString
        getHostnameBytes() {
      Object ref = hostname_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (String) ref);
        hostname_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>optional string hostname = 2;</code>
     */
    public Builder setHostname(
        String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      hostname_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>optional string hostname = 2;</code>
     */
    public Builder clearHostname() {
      
      hostname_ = getDefaultInstance().getHostname();
      onChanged();
      return this;
    }
    /**
     * <code>optional string hostname = 2;</code>
     */
    public Builder setHostnameBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      hostname_ = value;
      onChanged();
      return this;
    }

    private Object filenames_ = "";
    /**
     * <code>optional string filenames = 3;</code>
     */
    public String getFilenames() {
      Object ref = filenames_;
      if (!(ref instanceof String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        String s = bs.toStringUtf8();
        filenames_ = s;
        return s;
      } else {
        return (String) ref;
      }
    }
    /**
     * <code>optional string filenames = 3;</code>
     */
    public com.google.protobuf.ByteString
        getFilenamesBytes() {
      Object ref = filenames_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (String) ref);
        filenames_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>optional string filenames = 3;</code>
     */
    public Builder setFilenames(
        String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      filenames_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>optional string filenames = 3;</code>
     */
    public Builder clearFilenames() {
      
      filenames_ = getDefaultInstance().getFilenames();
      onChanged();
      return this;
    }
    /**
     * <code>optional string filenames = 3;</code>
     */
    public Builder setFilenamesBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      filenames_ = value;
      onChanged();
      return this;
    }

    private long storedDataSize_ ;
    /**
     * <code>optional int64 storedDataSize = 4;</code>
     */
    public long getStoredDataSize() {
      return storedDataSize_;
    }
    /**
     * <code>optional int64 storedDataSize = 4;</code>
     */
    public Builder setStoredDataSize(long value) {
      
      storedDataSize_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>optional int64 storedDataSize = 4;</code>
     */
    public Builder clearStoredDataSize() {
      
      storedDataSize_ = 0L;
      onChanged();
      return this;
    }
    public final Builder setUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return this;
    }

    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return this;
    }


    // @@protoc_insertion_point(builder_scope:com.ybh.dfs.namenode.rpc.ReportCompleteStorageInfoRequest)
  }

  // @@protoc_insertion_point(class_scope:com.ybh.dfs.namenode.rpc.ReportCompleteStorageInfoRequest)
  private static final ReportCompleteStorageInfoRequest DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new ReportCompleteStorageInfoRequest();
  }

  public static ReportCompleteStorageInfoRequest getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<ReportCompleteStorageInfoRequest>
      PARSER = new com.google.protobuf.AbstractParser<ReportCompleteStorageInfoRequest>() {
    public ReportCompleteStorageInfoRequest parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
        return new ReportCompleteStorageInfoRequest(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<ReportCompleteStorageInfoRequest> parser() {
    return PARSER;
  }

  @Override
  public com.google.protobuf.Parser<ReportCompleteStorageInfoRequest> getParserForType() {
    return PARSER;
  }

  public ReportCompleteStorageInfoRequest getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

