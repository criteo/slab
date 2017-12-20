// @flow
import marked from 'marked';
import type { Check } from '../state';

type Props = {
  checks: Array<Check>
};

const CheckList = ({ checks }: Props) => (
  <section className="checks">
    {checks.map(({ title, status, message }: Check) => (
      <div className="check" key={title}>
        <span className={`status background ${status}`} />
        <div className="content">
          <h4>{title}</h4>
          <div dangerouslySetInnerHTML={ { __html: marked(message) } } />
        </div>
      </div>
    ))}
  </section>
);

export default CheckList;
