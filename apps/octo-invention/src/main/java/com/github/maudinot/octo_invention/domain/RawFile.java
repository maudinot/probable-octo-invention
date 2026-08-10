package com.github.maudinot.octo_invention.domain;

public record RawFile(byte[] bytes, String filename, String contentType, long size) {}
