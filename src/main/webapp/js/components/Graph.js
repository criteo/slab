// @flow
import { Component } from 'react';

import Box from './Box';
import type { Board } from '../state';

type Props = {
  board: Board
};

class Graph extends Component {
  props: Props;

  render() {
    const { board } = this.props;
    return (
      <div className='graph'>
        <header className={board.status}>
          <h1>{board.title}</h1>
          <p>{board.message}</p>
        </header>
        <main>
          {
            board.columns.map((col, i) => (
              <section className="column" key={i} style={ { 'flexBasis': `${col.percentage}%` } }>
                {
                  col.rows.map(row => (
                    <section className="row" key={row.title}>
                      <h2>{row.title}</h2>
                      {
                        row.boxes.map(box => (
                          <Box box={box} key={box.title}/>
                        ))
                      }
                    </section>
                  ))
                }
              </section>
            ))
          }
        </main>
      </div>
    );
  }
}

export default Graph;