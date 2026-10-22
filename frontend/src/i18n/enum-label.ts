/** 列挙値の表示名（T07 の属性名）を i18n から解決する。JOIN はせず、コードから表示名を引く。 */
import type { TFunction } from 'i18next'
import type { EnumName, EnumValueMap } from '@/types/enums'

/**
 * 列挙値の表示名を返す。
 *
 * @param t i18next の翻訳関数
 * @param enumName 列挙名（例: `OrderStatus`）
 * @param value 列挙値（例: `APPROVED`）
 * @returns 表示名。未登録なら値そのもの
 */
export function enumLabel<N extends EnumName>(
  t: TFunction,
  enumName: N,
  value: EnumValueMap[N],
): string {
  return t(`ui.enums.${enumName}.${value}`, { defaultValue: value })
}
