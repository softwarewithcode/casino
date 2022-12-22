package com.casino.common.table;

import java.util.UUID;

import com.casino.common.language.Language;

public record TableInitData(Thresholds thresholds, UUID id, Language language, Type tableType, Game game) {

}
