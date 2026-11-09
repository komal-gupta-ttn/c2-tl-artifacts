package com.ttn.c2.artifact2.rag.service;

import com.ttn.c2.artifact2.rag.model.DocumentChunk;
import org.springframework.stereotype.Service;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

/**
 * Document service — loads source docs from docs/sample_docs for indexing.
 */
@Service
public class DocumentService {

    public List<Map.Entry<String, String>> loadDocs(Path docsDir) throws IOException {
        List<Map.Entry<String, String>> docs = new ArrayList<>();
        if (!Files.exists(docsDir)) return docs;
        try (Stream<Path> stream = Files.walk(docsDir)) {
            stream.filter(Files::isRegularFile)
                  .filter(p -> {
                      String name = p.getFileName().toString().toLowerCase();
                      return name.endsWith(".md") || name.endsWith(".txt") || name.endsWith(".py");
                  })
                  .sorted()
                  .forEach(p -> {
                      try {
                          String rel = docsDir.relativize(p).toString();
                          docs.add(Map.entry(rel, Files.readString(p)));
                      } catch (IOException ignored) {}
                  });
        }
        return docs;
    }
}
