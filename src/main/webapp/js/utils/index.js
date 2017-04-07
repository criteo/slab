export * from './fetcher';

export const combineViewAndLayout = (view, layout, links = []) => {
  // map from box name to [columnIndex, rowIndex, boxIndex]
  const map = new Map();
  layout.columns.forEach((col, i) =>
    col.rows.forEach((row, j) =>
      row.boxes.forEach((box, k) => map.set(box, [i,j,k]))
    )
  );
  const result = JSON.parse(JSON.stringify(layout));
  view.boxes.map(box => {
    const [i, j, k] = map.get(box.title);
    // mutate result
    result.columns[i].rows[j].boxes[k] = {
      title: box.title,
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