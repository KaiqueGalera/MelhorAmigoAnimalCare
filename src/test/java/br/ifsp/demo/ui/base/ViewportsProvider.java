package br.ifsp.demo.ui.base;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.util.stream.Stream;

public class ViewportsProvider implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        return Stream.of(
                Arguments.of("Mobile pequeno", 320, 568),
                Arguments.of("Mobile", 375, 667),
                Arguments.of("Mobile grande", 414, 896),
                Arguments.of("Tablet retrato", 768, 1024),
                Arguments.of("Tablet paisagem", 1024, 768),
                Arguments.of("Laptop", 1366, 768),
                Arguments.of("Desktop Full HD", 1920, 1080)
        );
    }
}