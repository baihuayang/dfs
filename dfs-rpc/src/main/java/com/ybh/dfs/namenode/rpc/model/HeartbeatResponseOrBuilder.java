// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: NameNodeRpcModel.proto

package com.ybh.dfs.namenode.rpc.model;

public interface HeartbeatResponseOrBuilder extends
    // @@protoc_insertion_point(interface_extends:com.ybh.dfs.namenode.rpc.HeartbeatResponse)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>optional int32 status = 1;</code>
   */
  int getStatus();

  /**
   * <code>optional string commands = 2;</code>
   */
  String getCommands();
  /**
   * <code>optional string commands = 2;</code>
   */
  com.google.protobuf.ByteString
      getCommandsBytes();
}
