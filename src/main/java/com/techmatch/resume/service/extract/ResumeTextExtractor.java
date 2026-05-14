package com.techmatch.resume.service.extract;

public interface ResumeTextExtractor {

    boolean supports(String extension);

    String extract(byte[] content);
}
