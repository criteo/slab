/* @flow */

import React from 'react'
import { connect } from 'react-redux'
import { findDOMNode } from 'react-dom'
import * as Actions from './actions'

import type { State, Board, SLA, Check } from  './state'

type Props = {
  loading: boolean,
  board: ?Board,
  zoomCheck: (id: string) => void
}

class Graph extends React.Component {
  props: Props;

  constructor(props: Props) {
    super(props);
    (this:any).drawLines = this.drawLines.bind(this);
  }

  componentDidMount() {
    window.addEventListener('resize', this.drawLines);
    this.drawLines();
  }

  componentWillUnmount() {
    window.removeEventListener('resize', this.drawLines);
  }

  componentDidUpdate() {
    this.drawLines();
  }

  spline(from, to) {
    let fromBox = this.box(from), toBox = this.box(to);
    let a = fromBox.x < toBox.x ? fromBox : toBox, b = fromBox.x < toBox.x ? toBox : fromBox;
    let n = Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2)) * .75;
    return `M${a.x},${a.y} C${a.x + n},${a.y} ${b.x - n},${b.y} ${b.x},${b.y}`;
  }

  box(el) {
    return {
      x: el.getBoundingClientRect().left + (el.getBoundingClientRect().right - el.getBoundingClientRect().left) / 2,
      y: el.getBoundingClientRect().top + (el.getBoundingClientRect().bottom - el.getBoundingClientRect().top) / 2
    }
  }

  drawLines() {
    let lines = findDOMNode(this.refs.lines);
    if(lines && this.props.board) {
      document.title = this.props.board.title;
      lines.innerHTML = this.props.board.sla.dependencies
        .map(([from, to]) => {
          return [findDOMNode(this.refs[from]), findDOMNode(this.refs[to])]
        })
        .filter(([to, from]) => to && from)
        .reduce((html, [to, from]) => {
          return html + `
            <g>
              <path d="${this.spline(from, to)}" class="bgLine" />
              <path d="${this.spline(from, to)}" class="fgLine" />
            </g>
          `
        }, '');
    }
  }

  renderGraph(sla: SLA) {
    return (
      <main>
        <svg ref="lines"></svg>
        {sla.checks.map((group,i) => {
          return (
            <section className="group" key={i} style={{flex: Math.max(...group.subgroups.map((sg, i) => {return sg.checks.length }))}}>
              { group.subgroups.map((subgroup, i) => {
                return (
                <section className="subgroup" key={i} style={{flex: Math.max(...subgroup.checks.map ((x, i) => {return x.length}))}}>
                  <h2>{subgroup.name}</h2>
                  {subgroup.checks.map((column,i) => {
                    return (
                      <section key={i} className="column">
                        {column.map((check,i) => {
                          return (
                            <div
                              ref={check.id}
                              key={check.id}
                              className={`check ${check.status}`}
                              onClick={this.props.zoomCheck.bind(this, check.id)}
>
                              <div>
                                <h3>{check.title}</h3>
                                {check.message ? <strong>{check.message}</strong> : null}
                                <div className="sub-check">
                                  {check.sub_checks.map((c, i) => {
                                    return (
                                      <span className={`${c.status}`}>
                                        {c.title}
                                      </span>
                                    )
                                  })}
                                </div>
                              </div>
                            </div>
                          )
                        })}
                      </section>
                    )
                  })}
                </section>
              )
              })}
            </section>
          )
        })}
      </main>
    )
  }

  render() {
    if(this.props.board) {
      return (
        <div className={this.props.lostConnection ? 'lost-connection' : ''}>
          <header className={this.props.board.sla.status}>
            <h1>{this.props.board.title}</h1>
            <p>{this.props.board.sla.message}</p>
          </header>
          {this.renderGraph(this.props.board.sla)}
        </div>
      )
    }
    else {
      return (
        <div>
          <header>
            <h1>Loading...</h1>
          </header>
        </div>
      )
    }
  }
}

function actions(dispatch, ownProps: Props) {
  return {
    ...ownProps,
    zoomCheck(checkId: string) {
      dispatch(Actions.zoomCheck(checkId))
    }
  }
}

function select(state: State, ownProps: Props): Props {
  return {
    ...ownProps,
    loading: state.loading,
    board: state.board,
    lostConnection: state.lostConnection
  }
}

export default connect(select, actions)(Graph)
