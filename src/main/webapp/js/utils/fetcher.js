const resolve = res => {
  const contentType = res.headers.get('content-type') || '';
  if (contentType.includes('json'))
    return res.json();
  if (contentType.includes('text'))
    return res.text();
  return Promise.reject(new TypeError(`unknown contentType ${contentType}`));
};

const fetcher = (url, options) =>
fetch(url, options)
.then(
  res => Promise.all([resolve(res), Promise.resolve(res)]),
  () => Promise.reject(Response.error())
)
.then(([body, res]) => {
  const response = {
    body,
    _res: res,
    status: res.status
  };
  if (res.status < 400)
    return Promise.resolve(response);
  else
    return Promise.reject(response);
});

export { fetcher };