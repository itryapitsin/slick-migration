package slick.migration.dialect

import slick.migration.ast.{ColumnInfo, IndexInfo, TableInfo}

import scala.slick.driver.DerbyDriver

/**
 * Created by Iliya Tryapitsin on 20/01/15.
 */
class DerbyDialect(driver: DerbyDriver) extends Dialect[DerbyDriver](driver) {
  override def autoInc(ci: ColumnInfo) =
    if (ci.autoInc) " GENERATED BY DEFAULT AS IDENTITY" else ""

  override def alterColumnType(table: TableInfo, column: ColumnInfo) = {
    val tmpColumnName = "tempcolumn" + (math.random * 1000000).toInt
    val tmpColumn = column.copy(name = tmpColumnName)
    List(
      addColumn(table, tmpColumn),
      s"update ${quoteTableName(table)} set ${quoteIdentifier(tmpColumnName)} = ${quoteIdentifier(column.name)}",
      dropColumn(table, column.name),
      renameColumn(table, tmpColumn, column.name)
    )
  }

  override def renameColumn(table: TableInfo, from: ColumnInfo, to: String) =
    s"rename column ${quoteTableName(table)}.${quoteIdentifier(from.name)} to ${quoteIdentifier(to)}"

  override def alterColumnNullability(table: TableInfo, column: ColumnInfo) =
    s"""alter table ${quoteTableName(table)}
      | alter column ${quoteIdentifier(column.name)}
      | ${if (column.notNull) "not" else ""} null""".stripMargin

  override def renameTable(table: TableInfo, to: String) =
    s"rename table ${quoteTableName(table)} to ${quoteIdentifier(to)}"

  override def renameIndex(old: IndexInfo, newName: String) = List(
    s"rename index ${quoteIdentifier(old.name)} to ${quoteIdentifier(newName)}"
  )
}
