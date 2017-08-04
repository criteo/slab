// @flow
// transfrom data from APIs
import _ from 'lodash';
import moment from 'moment';
import type { Stats, StatsEntry, BoardView, Layout } from '../state';

// combine board view and board layout
export const combineViewAndLayout = (view: any, layout: Layout, links: Array<string> = []): BoardView => {
  // map from box name to [columnIndex, rowIndex, boxIndex]
  const map = new Map();
  layout.columns.forEach((col, i) =>
    col.rows.forEach((row, j) =>
      row.boxes.forEach((box, k) => map.set(box.title, [i,j,k]))
    )
  );
  const result = JSON.parse(JSON.stringify(layout));
  result.columns.forEach(col =>
    col.rows.forEach(row =>
      row.boxes.forEach(box => {
        box.status = 'Unknown';
        box.message = 'Unknown';
      })
    )
  );
  view.boxes.map(box => {
    const [i, j, k] = map.get(box.title) || [];
    const _box = result.columns[i].rows[j].boxes[k];
    // mutate result
    result.columns[i].rows[j].boxes[k] = {
      ..._box,
      status: box.status,
      message: box.message,
      checks: box.checks
    };
  });
  return {
    ...result,
    title: view.title,
    message: view.message,
    status: view.status,
    links
  };
};

// aggregate hourly statistics from API by local day
export const aggregateStatsByDay = (stats: Stats): Stats =>
  _(stats)
    .toPairs()
    .groupBy(pair => moment(parseInt(pair[0])).startOf('day').valueOf())
    .mapValues(entries => _.reduce(entries, (acc: StatsEntry, [_, entry: StatsEntry]) => ({
      successes: acc.successes + entry.successes,
      warnings: acc.warnings + entry.warnings,
      errors: acc.errors + entry.errors,
      unknown: acc.unknown + entry.unknown,
      total: acc.total + entry.total
    }), { successes: 0, warnings: 0, errors: 0, unknown: 0, total: 0}))
    .value();