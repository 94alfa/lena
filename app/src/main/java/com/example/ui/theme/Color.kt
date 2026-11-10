package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// =========================================================================
// Paleta Oficial DopaTap M3 - Tons Calmos & Profissionais
// =========================================================================

// Primária Teal (Calma e Equilíbrio)
val Teal700 = Color(0xFF0F766E)       // Primária Claro
val Teal300 = Color(0xFF5EEAD4)       // Primária Escuro
val Teal900 = Color(0xFF042F2E)       // Sobre primária escuro
val Teal100 = Color(0xFFCCFBF1)       // Container primário claro
val Teal800 = Color(0xFF134E4A)       // Container primário escuro

// Fundo e Superfícies
val BgLight = Color(0xFFF8FAFC)        // Fundo Claro sereno
val BgDark = Color(0xFF0B1220)         // Fundo Escuro profundo
val SurfaceLight = Color(0xFFFFFFFF)   // Cartões Claro
val SurfaceDark = Color(0xFF111A2B)    // Cartões Escuro
val OutlineLight = Color(0xFFE2E8F0)   // Borda 1dp Claro
val OutlineDark = Color(0xFF273449)    // Borda 1dp Escuro

// Textos com Contraste WCAG AA / AAA
val TextPrimaryLight = Color(0xFF0F172A)
val TextPrimaryDark = Color(0xFFE6EDF7)
val TextSecondaryLight = Color(0xFF475569)
val TextSecondaryDark = Color(0xFFA3B1C6)
val TextMuted = Color(0xFF94A3B8)

// Cores Semânticas com Moderação
val AmberWarningLight = Color(0xFFB45309)      // Picos, Pausa recomendada
val AmberWarningDark = Color(0xFFFBBF24)
val AmberContainerLight = Color(0xFFFEF3C7)   // Fundo do cartão de pausa
val AmberContainerDark = Color(0xFF3A2A08)

val GreenSuccessLight = Color(0xFF15803D)      // Meta cumprida, foco bom
val GreenSuccessDark = Color(0xFF4ADE80)
val GreenContainerLight = Color(0xFFDCFCE7)
val GreenContainerDark = Color(0xFF14532D)

val RedErrorLight = Color(0xFFB91C1C)          // Apenas erros reais
val RedErrorDark = Color(0xFFF87171)

// Gráficos (Máximo 3 cores: Teal principal, Cinza-azulado secundário, Âmbar picos)
val ChartSeriesPrimaryLight = Teal700
val ChartSeriesPrimaryDark = Teal300
val ChartSeriesSecondary = Color(0xFF94A3B8)
val ChartSeriesWarningLight = AmberWarningLight
val ChartSeriesWarningDark = AmberWarningDark

// =========================================================================
// Aliases para compatibilidade reversa temporária
// =========================================================================
val WarmWhite = BgLight
val ClearWhite = SurfaceLight
val SoftSlate = TextPrimaryLight
val SlateMuted = TextSecondaryLight
val LightBorder = OutlineLight
val DopamineGold = Teal700
val SoftBlue = TextSecondaryLight
val MintGreen = GreenSuccessLight
val CoralWarning = AmberWarningLight
val SoftRose = AmberContainerLight
val SoftCyan = Teal100
val RechartsBlue = Teal700
val RechartsPurple = ChartSeriesSecondary
val RechartsSpike = AmberWarningLight
val RechartsEmerald = GreenSuccessLight
val RechartsAmber = AmberWarningLight
val RechartsGrid = OutlineLight
val RechartsSurface = BgLight
val ZenDark = BgDark
