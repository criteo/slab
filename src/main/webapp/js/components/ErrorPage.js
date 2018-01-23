// @flow
type Props = {
  message: string
};
const ErrorPage = ({ message }: Props) => (
  <div className="error-page">
    {byMessage(message)}
  </div>
);

const byMessage = (message: string) => {
  if (!message)
    return <h1>Unknown error</h1>;
  else if (message.includes('is not ready'))
    return (
      <div>
        <h1>{message}</h1>
        <p>check <a href="https://github.com/criteo/slab/wiki/FAQ">this page</a> for explanations</p>
      </div>
    );
  else if (message.includes('does not exist'))
    return (
      <div>
        <h1>{message}</h1>
        <p>Return to <a href="/">board list</a></p>
      </div>
    );
  else
    return <h1>{message}</h1>;
};

export default ErrorPage;