# Refactoring Opportunities

| Local | Cheiro no Código | Refabricação | Nº Aluno |
|-------|-----------------|--------------|----------|
| HuggingFaceClient::buildGameHistory() | Long Method (CC=30, LOC=51) | Extract Method | 110323 |
| HuggingFaceClient::sendMessage() | Replace System.out by logger | Replace System.out by logger | 110323 |
| GameReportPDF::summariseMove() | Overly Complex Method (CC=12) | Extract Method | 110323 |
| Ship::buildShip() | Useless assignments (category, bearing, pos) | Remove redundant initializer | 110323 |
| Position::randomPosition() | Use Random.nextInt() instead | Substitute Algorithm | 110323 |
| Tasks | Missing private constructor | Add private constructor | 110323 |
| Ship.java (L227, L247, L267, L287, L307, L352, L375) | Suspicious indentation after if | Add Braces | 122466 |
| Ship::stillFloating() | Multiple return points | Replace Nested Conditional with Guard Clauses | 122466 |
| Ship::tooCloseTo() | Multiple return points | Replace Nested Conditional with Guard Clauses | 122466 |
| Move::processEnemyFire() | Redundant String.format() call | Inline | 122466 |
| Game::jsonString | Unused Assignment | Remove redundant initializer | 122466 |
| Position::equals() | Multiple return points (3) | Replace Nested Conditional with Guard Clauses | 122466 |
| Ship::buildShip() | Overly Coupled Method (7 classes) | Extract Method | 122466 |
| Game::printBoard() | Long Method + Overly Complex (CC=20) | Extract Method | 122475 |
| Game::fireSingleShot() | Multiple return points (4) | Replace Nested Conditional with Guard Clauses | 122475 |
| Game::randomEnemyFire() | Call to set.removeAll(list) slowly | Substitute Algorithm | 122475 |
| Fleet::shipAt() | Multiple return points | Replace Nested Conditional with Guard Clauses | 122475 |
| Fleet::colisionRisk() | Multiple return points | Replace Nested Conditional with Guard Clauses | 122475 |
| Tasks::readClassicPosition() | Multiple return points | Replace Nested Conditional with Guard Clauses | 122475 |
| HuggingFaceClient::buildGameHistory() | Overly Nested Method (depth=6) | Extract Method | 122475 |
| Move::processEnemyFire() | Long Method (CC=32, LOC=77) | Extract Method | 122994 |
| Tasks::menu() | Long Method (CC=27, LOC=92) | Extract Method | 122994 |
| Tasks::menu() | Brain Method (Cognitive Complexity=61) | Decompose Conditional | 122994 |
| Galleon::fillSouth() | Multiple loops | Extract Method | 122994 |
| Ship::occupies() | Multiple return points | Replace Nested Conditional with Guard Clauses | 122994 |
| Caravel, Carrack, Frigate | Multiple loops (4 each) | Extract Method | 122994 |
| Game::fireSingleShot() | 4 negations | Invert Boolean | 122994 |