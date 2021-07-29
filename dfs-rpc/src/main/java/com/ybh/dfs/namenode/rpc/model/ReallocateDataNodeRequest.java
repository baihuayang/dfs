// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: NameNodeRpcModel.proto

package com.ybh.dfs.namenode.rpc.model;

/**
 * Protobuf type {@code com.ybh.dfs.namenode.rpc.ReallocateDataNodeRequest}
 */
public  final class ReallocateDataNodeRequest extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:com.ybh.dfs.namenode.rpc.ReallocateDataNodeRequest)
    ReallocateDataNodeRequestOrBuilder {
  // Use ReallocateDataNodeRequest.newBuilder() to construct.
  private ReallocateDataNodeRequest(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private ReallocateDataNodeRequest() {
    filename_ = "";
    filesize_ = 0L;
    excludeDataNodeId_ = "";
  }

  @Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return com.google.protobuf.UnknownFieldSet.getDefaultInstance();
  }
  private ReallocateDataNodeRequest(
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

            filename_ = s;
            break;
          }
          case 16: {

            filesize_ = input.readInt64();
            break;
          }
          case 26: {
            String s = input.readStringRequireUtf8();

            excludeDataNodeId_ = s;
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
    return NameNodeRpcModel.internal_static_com_ybh_dfs_namenode_rpc_ReallocateDataNodeRequest_descriptor;
  }

  protected FieldAccessorTable
      internalGetFieldAccessorTable() {
    return NameNodeRpcModel.internal_static_com_ybh_dfs_namenode_rpc_ReallocateDataNodeRequest_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            ReallocateDataNodeRequest.class, Builder.class);
  }

  public static final int FILENAME_FIELD_NUMBER = 1;
  private volatile Object filename_;
  /**
   * <code>optional string filename = 1;</code>
   */
  public String getFilename() {
    Object ref = filename_;
    if (ref instanceof String) {
      return (String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      String s = bs.toStringUtf8();
      filename_ = s;
      return s;
    }
  }
  /**
   * <code>optional string filename = 1;</code>
   */
  public com.google.protobuf.ByteString
      getFilenameBytes() {
    Object ref = filename_;
    if (ref instanceof String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (String) ref);
      filename_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int FILESIZE_FIELD_NUMBER = 2;
  private long filesize_;
  /**
   * <code>optional int64 filesize = 2;</code>
   */
  public long getFilesize() {
    return filesize_;
  }

  public static final int EXCLUDEDATANODEID_FIELD_NUMBER = 3;
  private volatile Object excludeDataNodeId_;
  /**
   * <code>optional string excludeDataNodeId = 3;</code>
   */
  public String getExcludeDataNodeId() {
    Object ref = excludeDataNodeId_;
    if (ref instanceof String) {
      return (String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      String s = bs.toStringUtf8();
      excludeDataNodeId_ = s;
      return s;
    }
  }
  /**
   * <code>optional string excludeDataNodeId = 3;</code>
   */
  public com.google.protobuf.ByteString
      getExcludeDataNodeIdBytes() {
    Object ref = excludeDataNodeId_;
    if (ref instanceof String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (String) ref);
      excludeDataNodeId_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
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
    if (!getFilenameBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1, filename_);
    }
    if (filesize_ != 0L) {
      output.writeInt64(2, filesize_);
    }
    if (!getExcludeDataNodeIdBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 3, excludeDataNodeId_);
    }
  }

  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (!getFilenameBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, filename_);
    }
    if (filesize_ != 0L) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt64Size(2, filesize_);
    }
    if (!getExcludeDataNodeIdBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(3, excludeDataNodeId_);
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
    if (!(obj instanceof ReallocateDataNodeRequest)) {
      return super.equals(obj);
    }
    ReallocateDataNodeRequest other = (ReallocateDataNodeRequest) obj;

    boolean result = true;
    result = result && getFilename()
        .equals(other.getFilename());
    result = result && (getFilesize()
        == other.getFilesize());
    result = result && getExcludeDataNodeId()
        .equals(other.getExcludeDataNodeId());
    return result;
  }

  @Override
  public int hashCode() {
    if (memoizedHashCode != 0) {
      return memoizedHashCode;
    }
    int hash = 41;
    hash = (19 * hash) + getDescriptorForType().hashCode();
    hash = (37 * hash) + FILENAME_FIELD_NUMBER;
    hash = (53 * hash) + getFilename().hashCode();
    hash = (37 * hash) + FILESIZE_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
        getFilesize());
    hash = (37 * hash) + EXCLUDEDATANODEID_FIELD_NUMBER;
    hash = (53 * hash) + getExcludeDataNodeId().hashCode();
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static ReallocateDataNodeRequest parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static ReallocateDataNodeRequest parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static ReallocateDataNodeRequest parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static ReallocateDataNodeRequest parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static ReallocateDataNodeRequest parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static ReallocateDataNodeRequest parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static ReallocateDataNodeRequest parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static ReallocateDataNodeRequest parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static ReallocateDataNodeRequest parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static ReallocateDataNodeRequest parseFrom(
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
  public static Builder newBuilder(ReallocateDataNodeRequest prototype) {
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
   * Protobuf type {@code com.ybh.dfs.namenode.rpc.ReallocateDataNodeRequest}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:com.ybh.dfs.namenode.rpc.ReallocateDataNodeRequest)
      ReallocateDataNodeRequestOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return NameNodeRpcModel.internal_static_com_ybh_dfs_namenode_rpc_ReallocateDataNodeRequest_descriptor;
    }

    protected FieldAccessorTable
        internalGetFieldAccessorTable() {
      return NameNodeRpcModel.internal_static_com_ybh_dfs_namenode_rpc_ReallocateDataNodeRequest_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              ReallocateDataNodeRequest.class, Builder.class);
    }

    // Construct using com.ybh.dfs.namenode.rpc.model.ReallocateDataNodeRequest.newBuilder()
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
      filename_ = "";

      filesize_ = 0L;

      excludeDataNodeId_ = "";

      return this;
    }

    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return NameNodeRpcModel.internal_static_com_ybh_dfs_namenode_rpc_ReallocateDataNodeRequest_descriptor;
    }

    public ReallocateDataNodeRequest getDefaultInstanceForType() {
      return ReallocateDataNodeRequest.getDefaultInstance();
    }

    public ReallocateDataNodeRequest build() {
      ReallocateDataNodeRequest result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    public ReallocateDataNodeRequest buildPartial() {
      ReallocateDataNodeRequest result = new ReallocateDataNodeRequest(this);
      result.filename_ = filename_;
      result.filesize_ = filesize_;
      result.excludeDataNodeId_ = excludeDataNodeId_;
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
      if (other instanceof ReallocateDataNodeRequest) {
        return mergeFrom((ReallocateDataNodeRequest)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(ReallocateDataNodeRequest other) {
      if (other == ReallocateDataNodeRequest.getDefaultInstance()) return this;
      if (!other.getFilename().isEmpty()) {
        filename_ = other.filename_;
        onChanged();
      }
      if (other.getFilesize() != 0L) {
        setFilesize(other.getFilesize());
      }
      if (!other.getExcludeDataNodeId().isEmpty()) {
        excludeDataNodeId_ = other.excludeDataNodeId_;
        onChanged();
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
      ReallocateDataNodeRequest parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (ReallocateDataNodeRequest) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }

    private Object filename_ = "";
    /**
     * <code>optional string filename = 1;</code>
     */
    public String getFilename() {
      Object ref = filename_;
      if (!(ref instanceof String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        String s = bs.toStringUtf8();
        filename_ = s;
        return s;
      } else {
        return (String) ref;
      }
    }
    /**
     * <code>optional string filename = 1;</code>
     */
    public com.google.protobuf.ByteString
        getFilenameBytes() {
      Object ref = filename_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (String) ref);
        filename_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>optional string filename = 1;</code>
     */
    public Builder setFilename(
        String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      filename_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>optional string filename = 1;</code>
     */
    public Builder clearFilename() {
      
      filename_ = getDefaultInstance().getFilename();
      onChanged();
      return this;
    }
    /**
     * <code>optional string filename = 1;</code>
     */
    public Builder setFilenameBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      filename_ = value;
      onChanged();
      return this;
    }

    private long filesize_ ;
    /**
     * <code>optional int64 filesize = 2;</code>
     */
    public long getFilesize() {
      return filesize_;
    }
    /**
     * <code>optional int64 filesize = 2;</code>
     */
    public Builder setFilesize(long value) {
      
      filesize_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>optional int64 filesize = 2;</code>
     */
    public Builder clearFilesize() {
      
      filesize_ = 0L;
      onChanged();
      return this;
    }

    private Object excludeDataNodeId_ = "";
    /**
     * <code>optional string excludeDataNodeId = 3;</code>
     */
    public String getExcludeDataNodeId() {
      Object ref = excludeDataNodeId_;
      if (!(ref instanceof String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        String s = bs.toStringUtf8();
        excludeDataNodeId_ = s;
        return s;
      } else {
        return (String) ref;
      }
    }
    /**
     * <code>optional string excludeDataNodeId = 3;</code>
     */
    public com.google.protobuf.ByteString
        getExcludeDataNodeIdBytes() {
      Object ref = excludeDataNodeId_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (String) ref);
        excludeDataNodeId_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>optional string excludeDataNodeId = 3;</code>
     */
    public Builder setExcludeDataNodeId(
        String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      excludeDataNodeId_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>optional string excludeDataNodeId = 3;</code>
     */
    public Builder clearExcludeDataNodeId() {
      
      excludeDataNodeId_ = getDefaultInstance().getExcludeDataNodeId();
      onChanged();
      return this;
    }
    /**
     * <code>optional string excludeDataNodeId = 3;</code>
     */
    public Builder setExcludeDataNodeIdBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      excludeDataNodeId_ = value;
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


    // @@protoc_insertion_point(builder_scope:com.ybh.dfs.namenode.rpc.ReallocateDataNodeRequest)
  }

  // @@protoc_insertion_point(class_scope:com.ybh.dfs.namenode.rpc.ReallocateDataNodeRequest)
  private static final ReallocateDataNodeRequest DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new ReallocateDataNodeRequest();
  }

  public static ReallocateDataNodeRequest getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<ReallocateDataNodeRequest>
      PARSER = new com.google.protobuf.AbstractParser<ReallocateDataNodeRequest>() {
    public ReallocateDataNodeRequest parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
        return new ReallocateDataNodeRequest(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<ReallocateDataNodeRequest> parser() {
    return PARSER;
  }

  @Override
  public com.google.protobuf.Parser<ReallocateDataNodeRequest> getParserForType() {
    return PARSER;
  }

  public ReallocateDataNodeRequest getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

